package pl.allegro.tech.build.execution.ci.github

import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import pl.allegro.tech.build.execution.ApiAccess
import pl.allegro.tech.build.execution.HeaderCredential
import pl.allegro.tech.build.execution.ProviderUnavailableException

class GitHubApiClientTest {
    @Test
    fun `malformed JSON becomes a provider failure with its parsing cause`() {
        withServer("{") { access ->
            GitHubApiClient(access).use { client ->
                val failure = assertFailsWith<ProviderUnavailableException> { client.tags() }
                assertEquals("GitHub tag response could not be parsed", failure.message)
                assertNotNull(failure.cause)
            }
        }
    }

    @Test
    fun `successful empty response remains distinct from a failed HTTP request`() {
        withServer("[]") { access ->
            GitHubApiClient(access).use { assertEquals(emptyList(), it.tags()) }
        }
        withServer("", status = 403) { access ->
            GitHubApiClient(access).use { client ->
                val failure = assertFailsWith<ProviderUnavailableException> { client.tags() }
                assertEquals("GitHub tag request failed with HTTP 403", failure.message)
            }
        }
    }

    @Test
    fun `transport failure becomes a provider failure with its IO cause`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/tags") { exchange -> exchange.close() }
        server.start()
        try {
            GitHubApiClient(access(server)).use { client ->
                val failure = assertFailsWith<ProviderUnavailableException> { client.tags() }
                assertEquals("GitHub tag request could not be completed", failure.message)
                assertIs<IOException>(failure.cause)
            }
        } finally {
            server.stop(0)
        }
    }

    private fun withServer(body: String, status: Int = 200, action: (ApiAccess) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/tags") { exchange ->
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            action(access(server))
        } finally {
            server.stop(0)
        }
    }

    private fun access(server: HttpServer): ApiAccess = ApiAccess(
        URI.create("http://127.0.0.1:${server.address.port}/tags"), HeaderCredential("Authorization", "Bearer fixture-token"),
    )
}
