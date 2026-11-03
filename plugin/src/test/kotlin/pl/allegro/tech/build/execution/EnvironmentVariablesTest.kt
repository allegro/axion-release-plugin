package pl.allegro.tech.build.execution

import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.execution.ci.github.GitHubContext
import pl.allegro.tech.build.execution.ci.gitlab.GitLabContext
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata

class EnvironmentVariablesTest {
    @Test
    fun `provider bindings normalize metadata endpoints and ordered token aliases`() {
        val githubValues = mapOf(
            "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_NAME" to "push", "GITHUB_REF_TYPE" to "branch",
            "GITHUB_REF_NAME" to "main", "GITHUB_SHA" to "revision", "GITHUB_EVENT_PATH" to payload(),
            "GITHUB_REPOSITORY" to "example/library", "GITHUB_API_URL" to "https://github.example/api/v3/",
            "GH_TOKEN" to "preferred", "GITHUB_TOKEN" to "fallback",
        )
        val github = assertIs<GitHubContext>(read(githubValues))
        val gitlab = assertIs<GitLabContext>(read(mapOf(
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
                github.selectTags("v", TagLookup.NEAREST_REACHABLE, offline = false)
            }.message,
        )
        assertEquals(
            "GitLab provider tag resolution is not supported yet",
            assertFailsWith<ProviderUnavailableException> {
                gitlab.selectTags("v", TagLookup.HIGHEST_GLOBAL, offline = false)
            }.message,
        )
        assertEquals(
            "Offline mode prevents CI tag API lookup",
            assertFailsWith<ProviderUnavailableException> {
                github.selectTags("v", TagLookup.HIGHEST_GLOBAL, offline = true)
            }.message,
        )

        val githubApi = github.apiAccess()
        assertEquals("https://github.example/api/v3/repos/example/library/tags", githubApi.tagsEndpoint.toString())
        assertEquals("Authorization", githubApi.credential.name)
        assertEquals("Bearer preferred", githubApi.credential.value)
        assertFalse(github.toString().contains("preferred"), "Context diagnostics must redact credentials")
        val fallback = assertIs<GitHubContext>(read(githubValues + ("GH_TOKEN" to "")))
        assertEquals("Bearer fallback", fallback.apiAccess().credential.value)
        val withoutToken = assertIs<GitHubContext>(read(githubValues - "GH_TOKEN" - "GITHUB_TOKEN"))
        assertEquals(
            "GitHub API lookup requires GH_TOKEN or GITHUB_TOKEN with contents: read",
            assertFailsWith<ProviderUnavailableException> {
                withoutToken.selectTags("v", TagLookup.HIGHEST_GLOBAL, offline = false)
            }.message,
        )
    }

    @Test
    fun `unavailable CI metadata falls back locally and inactive providers do not require metadata`() {
        val values = mapOf(
            "CI" to "true", "PATH" to "/usr/bin", "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_PATH" to missingPayload(),
        )
        val local = assertIs<ExecutionContext.Local>(read(values))
        assertEquals("/usr/bin", local.executablePath)
        assertEquals("GitHub event payload could not be read", local.fallbackFailure?.message)
        assertNotNull(local.fallbackFailure?.cause)

        assertIs<ExecutionContext.Local>(read(mapOf("GITHUB_ACTIONS" to "false", "GITLAB_CI" to "false")))
    }

    @Test
    fun `GitHub normalizes malformed and unreadable event payloads before context fallback`() {
        val malformed = assertIs<ExecutionContext.Local>(read(mapOf(
            "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_PATH" to payload("{"),
        )))
        assertEquals("GitHub event payload could not be parsed", malformed.fallbackFailure?.message)
        assertNotNull(malformed.fallbackFailure?.cause)

        val unreadable = assertIs<ExecutionContext.Local>(read(mapOf(
            "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_PATH" to directoryPayload(),
        )))
        assertEquals("GitHub event payload could not be read", unreadable.fallbackFailure?.message)
        assertIs<IOException>(unreadable.fallbackFailure?.cause)
    }

    private fun read(values: Map<String, String>): ExecutionContext = ExecutionContexts.resolve(values)

    private fun payload(content: String = """{"repository":{"default_branch":"main"}}"""): String =
        File.createTempFile("tagline-event", ".json").apply {
            deleteOnExit()
            writeText(content)
        }.absolutePath

    private fun missingPayload(): String = File.createTempFile("tagline-missing", ".json").apply {
        deleteOnExit()
        delete()
    }.absolutePath

    private fun directoryPayload(): String = File.createTempFile("tagline-directory", "").apply {
        deleteOnExit()
        delete()
        mkdirs()
    }.absolutePath
}
