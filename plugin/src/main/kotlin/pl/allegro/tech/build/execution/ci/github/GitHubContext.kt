package pl.allegro.tech.build.execution.ci.github

import java.io.File
import java.io.IOException
import java.net.URI
import pl.allegro.tech.build.execution.ApiAccess
import pl.allegro.tech.build.execution.EnvironmentField
import pl.allegro.tech.build.execution.EnvironmentVariables
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.HeaderCredential
import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.execution.ci.ExecutionContextResolver
import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection
import pl.allegro.tech.build.versioning.highestVersionTag
import pl.allegro.tech.build.versioning.versionTag

internal data class GitHubContext(
    override val metadata: RepositoryMetadata,
    override val event: EventTrigger,
    override val fallback: ExecutionContext.Local,
    val repository: String?,
    val apiUrl: String,
    val credential: HeaderCredential?,
) : ExecutionContext.Ci {

    override fun resolveTags(prefix: String, tagLookup: TagLookup, offline: Boolean): TagSelection {
        val eventTag = event.currentTag
        if (eventTag != null) {
            requireNotNull(versionTag(eventTag, prefix)) {
                "GitHub event tag '$eventTag' must match prefix '$prefix' and contain a SemVer version"
            }
            return TagSelection(eventTag, eventTag)
        }
        if (tagLookup != TagLookup.HIGHEST_GLOBAL) {
            throw ProviderUnavailableException("GitHub $tagLookup lookup is not supported yet")
        }
        if (offline) throw ProviderUnavailableException("Offline mode prevents CI tag API lookup")
        val tags = GitHubApiClient(apiAccess()).use { it.tags() }
        val exact = highestVersionTag(tags.filter { it.commit == metadata.revision }.map { it.name }, prefix)
        val base = exact ?: highestVersionTag(tags.map { it.name }, prefix)
        return TagSelection(exact?.name, base?.name)
    }

    fun apiAccess(): ApiAccess {
        val repository = repository
            ?: throw ProviderUnavailableException("GitHub branch builds require GITHUB_REPOSITORY")
        val credential = credential
            ?: throw ProviderUnavailableException("GitHub API lookup requires GH_TOKEN or GITHUB_TOKEN with contents: read")
        return ApiAccess(URI.create("${apiUrl.trimEnd('/')}/repos/$repository/tags"), credential)
    }

    companion object : ExecutionContextResolver {
        override val variables: Map<EnvironmentField, List<String>> = mapOf(
            EnvironmentField.CI_HINT to listOf("GITHUB_ACTIONS"),
            EnvironmentField.EVENT_NAME to listOf("GITHUB_EVENT_NAME"),
            EnvironmentField.REF_TYPE to listOf("GITHUB_REF_TYPE"),
            EnvironmentField.REF_NAME to listOf("GITHUB_REF_NAME"),
            EnvironmentField.EVENT_PAYLOAD to listOf("GITHUB_EVENT_PATH"),
            EnvironmentField.REVISION to listOf("GITHUB_SHA"),
            EnvironmentField.REPOSITORY_ID to listOf("GITHUB_REPOSITORY"),
            EnvironmentField.API_URL to listOf("GITHUB_API_URL"),
            EnvironmentField.TOKEN to listOf("GH_TOKEN", "GITHUB_TOKEN"),
        )

        override fun resolve(
            environment: EnvironmentVariables,
            fallback: ExecutionContext.Local,
        ): GitHubContext {
            val payload = environment.maybeGet(EnvironmentField.EVENT_PAYLOAD)?.let(::readPayload)
            val events = GitHubEvents(
                environment.maybeGet(EnvironmentField.EVENT_NAME), environment.maybeGet(EnvironmentField.REF_TYPE),
                environment.maybeGet(EnvironmentField.REF_NAME), payload,
            )
            val tagged = events.event.currentTag != null
            val defaultBranch = events.defaultBranch
            if (!tagged && defaultBranch == null) {
                throw ProviderUnavailableException("GitHub branch builds require repository.default_branch in GITHUB_EVENT_PATH")
            }
            return GitHubContext(
                fallback = fallback,
                metadata = RepositoryMetadata(
                    revision = environment[EnvironmentField.REVISION],
                    branch = if (tagged) null else environment[EnvironmentField.REF_NAME],
                    defaultBranch = defaultBranch,
                ),
                event = events.event,
                repository = environment.maybeGet(EnvironmentField.REPOSITORY_ID),
                apiUrl = environment.maybeGet(EnvironmentField.API_URL) ?: "https://api.github.com",
                credential = environment.maybeGet(EnvironmentField.TOKEN)?.let { HeaderCredential("Authorization", "Bearer $it") },
            )
        }

        private fun readPayload(path: String): String = try {
            File(path).readText()
        } catch (failure: IOException) {
            throw ProviderUnavailableException("GitHub event payload could not be read", failure)
        }
    }
}
