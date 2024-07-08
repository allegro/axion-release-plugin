package pl.allegro.tech.build.environment;

import com.github.zafarkhaja.semver.Version;

public interface GitEnvironment {
    String getDefaultBranchName();

    String getCurrentBranchName();

    String getHeadCommit();

    Version getCurrentVersion();

    boolean isAhead();
}
