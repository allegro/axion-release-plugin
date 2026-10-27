package pl.allegro.tech.build.execution.ci.github

import java.io.IOException
import java.net.URI
import pl.allegro.tech.build.execution.ApiAccess
import pl.allegro.tech.build.execution.EnvironmentField
import pl.allegro.tech.build.execution.EnvironmentVariables
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.HeaderCredential
import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.execution.ci.CiProvider
import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

internal object GitHubProvider : CiProvider {
    data class Context(
        override val metadata: RepositoryMetadata,
        override val event: EventTrigger,
        val repository: String?,
        val apiUrl: String,
        val credential: HeaderCredential?,
        override val fallback: ExecutionContext.Local,
    ) : ExecutionContext.Ci {
        override fun resolveTags(prefix: String, tagLookup: TagLookup, offline: Boolean): TagSelection =
            GitHubTagReader(this, offline).resolveTags(prefix, tagLookup)

        fun apiAccess(): ApiAccess {
            val repository = repository
                ?: throw ProviderUnavailableException("GitHub branch builds require GITHUB_REPOSITORY")
            val credential = credential
                ?: throw ProviderUnavailableException("GitHub API lookup requires GH_TOKEN or GITHUB_TOKEN with contents: read")
            return ApiAccess(URI.create("${apiUrl.trimEnd('/')}/repos/$repository/tags"), credential)
        }
    }

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

    override fun read(
        environment: EnvironmentVariables,
        fallback: ExecutionContext.Local,
        readFile: (String) -> String,
    ): Context {
        val payload = try {
            environment.maybeGet(EnvironmentField.EVENT_PAYLOAD)?.let(readFile)
        } catch (failure: IOException) {
            throw ProviderUnavailableException("GitHub event payload could not be read", failure)
        }
        val events = GitHubEvents(
            environment.maybeGet(EnvironmentField.EVENT_NAME), environment.maybeGet(EnvironmentField.REF_TYPE),
            environment.maybeGet(EnvironmentField.REF_NAME), payload,
        )
        val tagged = events.event.currentTag != null
        val defaultBranch = events.defaultBranch
        if (!tagged && defaultBranch == null) {
            throw ProviderUnavailableException("GitHub branch builds require repository.default_branch in GITHUB_EVENT_PATH")
        }
        return Context(
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
}
