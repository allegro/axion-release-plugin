package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionContext {

    private final Version version;
    private final boolean snapshot;
    private final Version previousVersion;
    private final ScmPosition position;

    public VersionContext(Version version, boolean snapshot, Version previousVersion, ScmPosition position) {
        this.version = version;
        this.snapshot = snapshot;
        this.previousVersion = previousVersion;
        this.position = position;
    }

    public final Version getVersion() {
        return version;
    }

    public final boolean isSnapshot() {
        return snapshot;
    }

    public final Version getPreviousVersion() {
        return previousVersion;
    }

    public final ScmPosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "VersionContext{" +
            "version=" + version +
            ", snapshot=" + snapshot +
            ", previousVersion=" + previousVersion +
            ", position=" + position +
            '}';
    }
}
