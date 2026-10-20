package pl.allegro.tech.build.execution.context.ci.github

import tools.jackson.databind.json.JsonMapper
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.highestVersionTag
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal class GitHubApiClient(
    private val endpoint: URI,
    private val token: String,
) {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
    private val mapper = JsonMapper()

    fun highestVersionTag(prefix: String): VersionTag? {
        val pages = generateSequence(fetchPage(URI.create("$endpoint?per_page=100&page=1"))) { response ->
            nextPage(response)?.let(::fetchPage)
        }
        val names = pages.flatMap { response ->
            val tags = mapper.readTree(response.body())
            require(tags.isArray) { "GitHub tag response is not an array" }
            tags.asSequence().mapNotNull { it.path("name").asString().takeIf(String::isNotBlank) }
        }
        return highestVersionTag(names.asIterable(), prefix)
    }

    private fun fetchPage(uri: URI): HttpResponse<String> {
        val request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(2))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer $token")
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) {
            "GitHub tag request failed with HTTP ${response.statusCode()}"
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
