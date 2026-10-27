package pl.allegro.tech.build.execution

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.execution.ci.github.GitHubProvider
import pl.allegro.tech.build.execution.ci.gitlab.GitLabProvider
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata

class EnvironmentVariablesTest {
    @Test
    fun `provider bindings normalize metadata endpoints and ordered token aliases`() {
        val githubValues = mapOf(
            "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_NAME" to "push", "GITHUB_REF_TYPE" to "branch",
            "GITHUB_REF_NAME" to "main", "GITHUB_SHA" to "revision", "GITHUB_EVENT_PATH" to "event.json",
            "GITHUB_REPOSITORY" to "example/library", "GITHUB_API_URL" to "https://github.example/api/v3/",
            "GH_TOKEN" to "preferred", "GITHUB_TOKEN" to "fallback",
        )
        val github = assertIs<GitHubProvider.Context>(read(githubValues))
        val gitlab = assertIs<GitLabProvider.Context>(read(mapOf(
            "GITLAB_CI" to "true", "CI_PIPELINE_SOURCE" to "push", "CI_COMMIT_REF_NAME" to "main",
            "CI_COMMIT_SHA" to "revision", "CI_DEFAULT_BRANCH" to "main",
        )))
        assertEquals(RepositoryMetadata("revision", "main", "main"), github.metadata)
        assertEquals(github.metadata, gitlab.metadata)
        assertEquals(EventTrigger.BranchPush, github.event)
        assertEquals(github.event, gitlab.event)
        assertEquals(
            "GitHub NEAREST_REACHABLE lookup is not supported yet",
            assertFailsWith<ProviderUnavailableException> {
                github.resolveTags("v", TagLookup.NEAREST_REACHABLE, offline = false)
            }.message,
        )
        assertEquals(
            "GitLab provider tag resolution is not supported yet",
            assertFailsWith<ProviderUnavailableException> {
                gitlab.resolveTags("v", TagLookup.HIGHEST_GLOBAL, offline = false)
            }.message,
        )
        assertEquals(
            "Offline mode prevents CI tag API lookup",
            assertFailsWith<ProviderUnavailableException> {
                github.resolveTags("v", TagLookup.HIGHEST_GLOBAL, offline = true)
            }.message,
        )

        val githubApi = github.apiAccess()
        assertEquals("https://github.example/api/v3/repos/example/library/tags", githubApi.tagsEndpoint.toString())
        assertEquals("Authorization", githubApi.credential.name)
        assertEquals("Bearer preferred", githubApi.credential.value)
        assertFalse(github.toString().contains("preferred"), "Context diagnostics must redact credentials")
        val fallback = assertIs<GitHubProvider.Context>(read(githubValues + ("GH_TOKEN" to "")))
        assertEquals("Bearer fallback", fallback.apiAccess().credential.value)
        val withoutToken = assertIs<GitHubProvider.Context>(read(githubValues - "GH_TOKEN" - "GITHUB_TOKEN"))
        assertEquals(
            "GitHub API lookup requires GH_TOKEN or GITHUB_TOKEN with contents: read",
            assertFailsWith<ProviderUnavailableException> {
                withoutToken.resolveTags("v", TagLookup.HIGHEST_GLOBAL, offline = false)
            }.message,
        )
    }

    @Test
    fun `unavailable CI metadata falls back locally and inactive providers do not require metadata`() {
        val values = mapOf("CI" to "true", "PATH" to "/usr/bin", "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_PATH" to "missing.json")
        val failure = ProviderUnavailableException("Missing CI payload")
        val local = assertIs<ExecutionContext.Local>(read(values) { throw failure })
        assertEquals("/usr/bin", local.executablePath)
        assertSame(failure, local.fallbackFailure)

        assertIs<ExecutionContext.Local>(read(mapOf("GITHUB_ACTIONS" to "false", "GITLAB_CI" to "false")))
    }

    @Test
    fun `GitHub normalizes malformed and unreadable event payloads before context fallback`() {
        val values = mapOf("GITHUB_ACTIONS" to "true", "GITHUB_EVENT_PATH" to "event.json")
        val malformed = assertIs<ExecutionContext.Local>(read(values) { "{" })
        assertEquals("GitHub event payload could not be parsed", malformed.fallbackFailure?.message)
        assertNotNull(malformed.fallbackFailure?.cause)

        val unreadable = IOException("Fixture read failure")
        val missing = assertIs<ExecutionContext.Local>(read(values) { throw unreadable })
        assertEquals("GitHub event payload could not be read", missing.fallbackFailure?.message)
        assertSame(unreadable, missing.fallbackFailure?.cause)

        val bug = IllegalArgumentException("Unexpected provider failure")
        assertSame(bug, assertFailsWith<IllegalArgumentException> { read(values) { throw bug } })
    }

    private fun read(
        values: Map<String, String>,
        readFile: (String) -> String = { """{"repository":{"default_branch":"main"}}""" },
    ): ExecutionContext = ExecutionContexts.read(values, readFile)
}
