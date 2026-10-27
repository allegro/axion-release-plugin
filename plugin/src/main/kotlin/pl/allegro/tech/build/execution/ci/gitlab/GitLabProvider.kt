package pl.allegro.tech.build.execution.ci.gitlab

import pl.allegro.tech.build.execution.EnvironmentField
import pl.allegro.tech.build.execution.EnvironmentVariables
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.execution.ci.CiProvider
import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

internal object GitLabProvider : CiProvider {
    data class Context(
        override val metadata: RepositoryMetadata,
        override val event: EventTrigger,
        override val fallback: ExecutionContext.Local,
    ) : ExecutionContext.Ci {
        override fun resolveTags(prefix: String, tagLookup: TagLookup, offline: Boolean): TagSelection =
            throw ProviderUnavailableException("GitLab provider tag resolution is not supported yet")
    }

    override val variables: Map<EnvironmentField, List<String>> = mapOf(
        EnvironmentField.CI_HINT to listOf("GITLAB_CI"),
        EnvironmentField.COMMIT_TAG to listOf("CI_COMMIT_TAG"),
        EnvironmentField.EVENT_NAME to listOf("CI_PIPELINE_SOURCE"),
        EnvironmentField.DEFAULT_BRANCH to listOf("CI_DEFAULT_BRANCH"),
        EnvironmentField.REF_NAME to listOf("CI_COMMIT_REF_NAME"),
        EnvironmentField.REVISION to listOf("CI_COMMIT_SHA"),
    )

    override fun read(
        environment: EnvironmentVariables,
        fallback: ExecutionContext.Local,
        readFile: (String) -> String,
    ): Context {
        val event = GitLabEvents.detect(
            environment.maybeGet(EnvironmentField.COMMIT_TAG), environment.maybeGet(EnvironmentField.EVENT_NAME),
        )
        return Context(
            fallback = fallback,
            metadata = RepositoryMetadata(
                revision = environment[EnvironmentField.REVISION],
                branch = if (event.currentTag != null) null else environment[EnvironmentField.REF_NAME],
                defaultBranch = environment[EnvironmentField.DEFAULT_BRANCH],
            ),
            event = event,
        )
    }
}
