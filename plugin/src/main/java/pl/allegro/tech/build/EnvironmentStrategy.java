package pl.allegro.tech.build;

import pl.allegro.tech.build.environment.GitEnvironment;
import pl.allegro.tech.build.environment.GitHubRunnerEnvironment;
import pl.allegro.tech.build.environment.LocalEnvironment;

import java.util.Map;

/**
 * Strategy for determining the environment in which the plugin is running.
 */
class EnvironmentStrategy {
    public static GitEnvironment getEnvironment(Map<String, String> environmentVariables) {
        if (System.getenv("GITHUB_ACTIONS") != null) return new GitHubRunnerEnvironment(environmentVariables);
        if (System.getenv("GITLAB_CI") != null) return new GitHubRunnerEnvironment(environmentVariables);
        return new LocalEnvironment();
    }
}
