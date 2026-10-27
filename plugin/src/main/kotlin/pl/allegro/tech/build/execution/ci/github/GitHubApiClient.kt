package pl.allegro.tech.build.execution.ci.github

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import pl.allegro.tech.build.execution.ApiAccess
import pl.allegro.tech.build.execution.ProviderUnavailableException
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

internal class GitHubApiClient(
    private val access: ApiAccess,
) : AutoCloseable {
    private val endpoint = access.tagsEndpoint
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build()
    private val mapper = JsonMapper()

    override fun close() = client.close()

    fun tags(): List<GitHubTag> = try {
        readTags()
    } catch (failure: JacksonException) {
        throw ProviderUnavailableException("GitHub tag response could not be parsed", failure)
    } catch (failure: IOException) {
        throw ProviderUnavailableException("GitHub tag request could not be completed", failure)
    }

    private fun readTags(): List<GitHubTag> {
        val visited = mutableSetOf<URI>()
        fun page(uri: URI): HttpResponse<String> {
            if (!visited.add(uri)) throw ProviderUnavailableException("GitHub tags API returned a pagination cycle")
            return fetchPage(uri)
        }
        val pages = generateSequence(page(URI.create("$endpoint?per_page=100&page=1"))) { response ->
            nextPage(response)?.let(::page)
        }
        return pages.flatMap { response ->
            val tags = mapper.readTree(response.body())
            if (!tags.isArray) throw ProviderUnavailableException("GitHub tag response is not an array")
            tags.asSequence().map { tag ->
                val name = tag.path("name").asString()
                val commit = tag.path("commit").path("sha").asString()
                if (name.isNullOrBlank() || commit.isNullOrBlank()) {
                    throw ProviderUnavailableException("GitHub tag response is missing name or commit.sha")
                }
                GitHubTag(name, commit)
            }
        }.toList()
    }

    private fun fetchPage(uri: URI): HttpResponse<String> {
        if (uri.scheme != endpoint.scheme || uri.authority != endpoint.authority || uri.path != endpoint.path) {
            throw ProviderUnavailableException("GitHub tags pagination must stay on the repository tags endpoint")
        }
        val request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(2))
            .header("Accept", "application/vnd.github+json")
            .header(access.credential.name, access.credential.value)
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw ProviderUnavailableException("GitHub tag request failed with HTTP ${response.statusCode()}")
        }
        return response
    }

    private fun nextPage(response: HttpResponse<String>): URI? =
        response.headers().firstValue("Link").orElse("")
            .split(',')
            .firstOrNull { "rel=\"next\"" in it }
            ?.substringAfter('<', "")
            ?.substringBefore('>', "")
            ?.takeIf(String::isNotBlank)
            ?.let(response.request().uri()::resolve)
}
