package pl.allegro.tech.build

import pl.allegro.tech.build.environment.GitEnvironment
import pl.allegro.tech.build.environment.GitHubRunnerEnvironment
import pl.allegro.tech.build.environment.GitLabRunnerEnvironment
import pl.allegro.tech.build.environment.LocalEnvironment

/**
 * Strategy for determining the environment in which the plugin is running.
 */
internal object Adapters {
    fun selectAdapter(environmentVariables: Map<String, String>): GitEnvironment {
        if (environmentVariables["GITHUB_ACTIONS"] != null) return GitHubRunnerEnvironment(environmentVariables)
        if (environmentVariables["GITLAB_CI"] != null) return GitLabRunnerEnvironment(environmentVariables)
        return LocalEnvironment()
    }
}
