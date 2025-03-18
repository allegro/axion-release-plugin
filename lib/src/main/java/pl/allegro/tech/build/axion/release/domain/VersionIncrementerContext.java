package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionIncrementerContext {

    private final Version currentVersion;
    private final ScmPosition scmPosition;
    private final boolean isLegacyDefTagnameRepo;

    public VersionIncrementerContext(Version currentVersion, ScmPosition scmPosition, boolean isLegacyDefTagnameRepo) {
        this.currentVersion = currentVersion;
        this.scmPosition = scmPosition;
        this.isLegacyDefTagnameRepo = isLegacyDefTagnameRepo;
    }

    public VersionIncrementerContext(Version currentVersion, ScmPosition scmPosition) {
        this(currentVersion, scmPosition, false);
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    public ScmPosition getScmPosition() {
        return scmPosition;
    }

    public boolean isLegacyDefTagnameRepo() {
        return isLegacyDefTagnameRepo;
    }
}
