package pl.allegro.tech.build.environment;

import com.github.zafarkhaja.semver.Version;
import org.gradle.internal.impldep.com.google.gson.Gson;
import org.gradle.internal.impldep.com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

/**
 * Does not use shell git commands
 * Creates git tag using GitHub releases api
 * Outputs generated values to GITHUB_OUTPUTS
 */
public class GitHubRunnerEnvironment implements GitEnvironment {

    private final GitHubEvent event;
    private final Map<String, String> environmentVariables;

    public GitHubRunnerEnvironment(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        try {
            JsonReader reader = new JsonReader(new FileReader(environmentVariables.get("GITHUB_EVENT_PATH")));
            this.event = new Gson().fromJson(reader, GitHubEvent.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDefaultBranchName() {
        return event.repository.default_branch;
    }

    @Override
    public String getCurrentBranchName() {
        return environmentVariables.get("GITHUB_REF_NAME");
    }

    @Override
    public String getHeadCommit() {
        return environmentVariables.get("GITHUB_SHA");
    }

    @Override
    public Version getCurrentVersion() {
        return Version.parse("1.0.0");
    }

    @Override
    public boolean isAhead() {
        String type = environmentVariables.get("GITHUB_EVENT_NAME");
        if (type.equals("pull_request")) return true;
        return getCurrentBranchName().equals(getDefaultBranchName());
    }

    static class GitHubEvent {
        public Repository repository;

        static class Repository {
            public String default_branch;
        }
    }
}
