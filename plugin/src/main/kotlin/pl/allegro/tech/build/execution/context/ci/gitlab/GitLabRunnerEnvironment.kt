package pl.allegro.tech.build.execution.context.ci.gitlab

import pl.allegro.tech.build.EnvironmentVariables
import pl.allegro.tech.build.execution.context.GitEnvironment
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.ci.EventTrigger
import pl.allegro.tech.build.execution.context.local.LocalRepository
import pl.allegro.tech.build.extension.GitlabSpec
import java.net.URI

internal class GitLabRunnerEnvironment(
    private val environment: EnvironmentVariables,
    internal val options: GitlabSpec,
    private val local: LocalRepository,
) : GitEnvironment {
    internal val event: EventTrigger = GitLabEvents.detect(
        environment.maybeGet("CI_COMMIT_TAG"),
        environment.maybeGet("CI_PIPELINE_SOURCE"),
    )
    private val highestTag = selectTagLookup()

    override val defaultBranchName: String
        get() = environment["CI_DEFAULT_BRANCH"]

    override val currentBranchName: String
        get() = environment["CI_COMMIT_REF_NAME"]

    override val headCommit: String
        get() = environment["CI_COMMIT_SHA"]

    override fun highestVersionTag(prefix: String): VersionTag? = highestTag(prefix)

    override fun nearestVersionTag(prefix: String): VersionTag? = local.nearestVersionTag(prefix)

    override fun getTag(prefix: String, useHighestVersion: Boolean): VersionTag? =
        event.tag(prefix, useHighestVersion, this)

    private fun selectTagLookup(): (String) -> VersionTag? {
        val project = environment.maybeGet("CI_PROJECT_ID")?.takeIf(String::isNotBlank)
            ?: return local::highestVersionTag
        val jobToken = environment.maybeGet("CI_JOB_TOKEN")?.takeIf(String::isNotBlank)
        val privateToken = environment.maybeGet("GITLAB_TOKEN")?.takeIf(String::isNotBlank)
            ?: environment.maybeGet("GLAB_TOKEN")?.takeIf(String::isNotBlank)
        val token = jobToken ?: privateToken ?: return local::highestVersionTag

        val api = environment.maybeGet("CI_API_V4_URL") ?: "https://gitlab.com/api/v4"
        val header = if (jobToken != null) "JOB-TOKEN" else "PRIVATE-TOKEN"
        return GitLabApiClient(
            URI.create("$api/projects/$project/repository/tags"),
            header, token,
        )::highestVersionTag
    }
}
