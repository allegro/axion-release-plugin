package pl.allegro.tech.build.execution.context.ci.gitlab

import tools.jackson.databind.json.JsonMapper
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.highestVersionTag
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal class GitLabApiClient(
    private val endpoint: URI,
    private val credentialHeader: String,
    private val token: String,
) {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
    private val mapper = JsonMapper()

    fun highestVersionTag(prefix: String): VersionTag? {
        val pages = generateSequence(fetchPage("1")) { response ->
            response.headers().firstValue("X-Next-Page").orElse("")
                .takeIf(String::isNotBlank)
                ?.let(::fetchPage)
        }
        val names = pages.flatMap { response ->
            val tags = mapper.readTree(response.body())
            require(tags.isArray) { "GitLab tag response is not an array" }
            tags.asSequence().mapNotNull { it.path("name").asString().takeIf(String::isNotBlank) }
        }
        return highestVersionTag(names.asIterable(), prefix)
    }

    private fun fetchPage(page: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create("$endpoint?per_page=100&page=$page"))
            .timeout(Duration.ofSeconds(2))
            .header("Accept", "application/json")
            .header(credentialHeader, token)
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) {
            "GitLab tag request failed with HTTP ${response.statusCode()}"
        }
        return response
    }
}
