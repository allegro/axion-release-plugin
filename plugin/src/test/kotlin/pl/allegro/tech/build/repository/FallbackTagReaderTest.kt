package pl.allegro.tech.build.repository

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.extension.Backend
import pl.allegro.tech.build.extension.TagLookup

class FallbackTagReaderTest {
    @Test
    fun `successful empty lookup stays authoritative and does not create local reader`() {
        val empty = TagSelection(null, null)
        val reader = FallbackTagReader(
            preferred = TagReader { _, _ -> empty },
            fallback = { error("An empty API response must not trigger fallback") },
            reportFallback = { error("No fallback should be reported") },
        )
        assertEquals(empty, reader.resolveTags("v", TagLookup.HIGHEST_GLOBAL))
    }

    @Test
    fun `provider refusal reports its reason and uses the local reader`() {
        val localTags = TagSelection(null, "v1.2.3")
        for (reason in listOf("Reachable history is unavailable", "Offline mode prevents CI tag API lookup")) {
            val reported = mutableListOf<String?>()
            val reader = FallbackTagReader(
                preferred = TagReader { _, _ -> throw ProviderUnavailableException(reason) },
                fallback = { TagReader { prefix, lookup ->
                    assertEquals("v", prefix)
                    assertEquals(TagLookup.HIGHEST_REACHABLE, lookup)
                    localTags
                } },
                reportFallback = { reported.add(it.message) },
            )
            assertEquals(localTags, reader.resolveTags("v", TagLookup.HIGHEST_REACHABLE))
            assertEquals<List<String?>>(listOf(reason), reported)
        }
    }

    @Test
    fun `validation errors do not trigger fallback`() {
        val reader = FallbackTagReader(
            preferred = TagReader { prefix, _ ->
                require(prefix == "v") { "Invalid event tag prefix" }
                TagSelection("v1.2.3", "v1.2.3")
            },
            fallback = { error("Event validation must not trigger fallback") },
        )
        assertEquals("v1.2.3", reader.resolveTags("v", TagLookup.NEAREST_REACHABLE).exactTag)
        assertFailsWith<IllegalArgumentException> { reader.resolveTags("other", TagLookup.HIGHEST_GLOBAL) }
    }

    @Test
    fun `provider failure is reported and preserved when local construction or lookup fails`() {
        val providerFailure = ProviderUnavailableException("GitHub request failed with HTTP 403")
        val localFailures = listOf(IllegalStateException("No local repository"), IllegalStateException("Missing local history"))
        for ((index, localFailure) in localFailures.withIndex()) {
            val reported = mutableListOf<ProviderUnavailableException>()
            val reader = FallbackTagReader(
                preferred = TagReader { _, _ -> throw providerFailure },
                fallback = {
                    if (index == 0) throw localFailure
                    TagReader { _, _ -> throw localFailure }
                },
                reportFallback = reported::add,
            )
            assertSame(localFailure, assertFailsWith<IllegalStateException> { reader.resolveTags("v", TagLookup.HIGHEST_GLOBAL) })
            assertEquals(listOf(providerFailure), reported)
            assertEquals(listOf(providerFailure), localFailure.suppressed.toList())
        }
    }

    @Test
    fun `failed local metadata keeps the unavailable CI metadata cause`() {
        val directory = Files.createTempDirectory("tagline-missing-repository-").toFile()
        try {
            val providerFailure = ProviderUnavailableException("GitHub event payload could not be parsed")
            val context = ExecutionContext.Local(executablePath = null, fallbackFailure = providerFailure)
            val failure = assertFails { Adapters.metadataReader(Backend.JGIT, context, directory).readMetadata() }
            assertEquals(listOf(providerFailure), failure.suppressed.toList())
        } finally {
            directory.deleteRecursively()
        }
    }
}
