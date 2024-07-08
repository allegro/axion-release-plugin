package pl.allegro.tech.build.environment;

import com.github.zafarkhaja.semver.Version;
import org.gradle.internal.impldep.org.apache.commons.lang.NotImplementedException;

/**
 * Does not use shell git commands
 * Creates git tag using GitLab releases api
 */
public class GitLabRunnerEnvironment implements GitEnvironment {
    @Override
    public String getDefaultBranchName() {
        return System.getenv("CI_DEFAULT_BRANCH");
    }

    @Override
    public String getCurrentBranchName() {
        return System.getenv("CI_COMMIT_REF_NAME");
    }

    @Override
    public String getHeadCommit() {
        return System.getenv("CI_COMMIT_SHA");
    }

    @Override
    public Version getCurrentVersion() {
        return Version.parse("1.0.0");
    }

    @Override
    public boolean isAhead() {
        throw new NotImplementedException("Not implemented");
    }
}
