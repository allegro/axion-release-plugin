package pl.allegro.tech.build.execution.context.ci.github

import pl.allegro.tech.build.EnvironmentVariables
import pl.allegro.tech.build.execution.context.GitEnvironment
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.ci.EventTrigger
import pl.allegro.tech.build.execution.context.local.LocalRepository
import pl.allegro.tech.build.extension.GithubSpec
import java.net.URI

internal class GitHubRunnerEnvironment(
    private val environment: EnvironmentVariables,
    internal val options: GithubSpec,
    private val local: LocalRepository,
) : GitEnvironment {
    private val events = GitHubEvents(
        environment.maybeGet("GITHUB_EVENT_NAME"),
        environment.maybeGet("GITHUB_REF_TYPE"),
        environment.maybeGet("GITHUB_REF_NAME"),
        environment.maybeGet("GITHUB_EVENT_PATH"),
    )
    internal val event: EventTrigger = events.event
    private val highestTag = selectTagLookup()

    override val defaultBranchName: String
        get() = events.defaultBranch
            ?.takeIf(String::isNotBlank)
            ?: error("GitHub event does not provide the default branch")

    override val currentBranchName: String
        get() = environment["GITHUB_REF_NAME"]

    override val headCommit: String
        get() = environment["GITHUB_SHA"]

    override fun highestVersionTag(prefix: String): VersionTag? = highestTag(prefix)

    override fun nearestVersionTag(prefix: String): VersionTag? = local.nearestVersionTag(prefix)

    override fun getTag(prefix: String, useHighestVersion: Boolean): VersionTag? =
        event.tag(prefix, useHighestVersion, this)

    private fun selectTagLookup(): (String) -> VersionTag? {
        val repository = environment.maybeGet("GITHUB_REPOSITORY")?.takeIf(String::isNotBlank)
            ?: return local::highestVersionTag
        val token = environment.maybeGet("GH_TOKEN")?.takeIf(String::isNotBlank)
            ?: environment.maybeGet("GITHUB_TOKEN")?.takeIf(String::isNotBlank)
        if (token == null) return local::highestVersionTag

        val api = environment.maybeGet("GITHUB_API_URL") ?: "https://api.github.com"
        return GitHubApiClient(
            URI.create("$api/repos/$repository/tags"),
            token,
        )::highestVersionTag
    }
}
