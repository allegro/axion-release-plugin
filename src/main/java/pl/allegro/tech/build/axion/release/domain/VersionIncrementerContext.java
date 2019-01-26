package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionIncrementerContext {

    private final Version currentVersion;
    private final ScmPosition scmPosition;

    public VersionIncrementerContext(Version currentVersion, ScmPosition scmPosition) {
        this.currentVersion = currentVersion;
        this.scmPosition = scmPosition;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    public ScmPosition getScmPosition() {
        return scmPosition;
    }
}
