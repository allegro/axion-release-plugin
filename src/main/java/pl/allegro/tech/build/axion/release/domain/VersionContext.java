package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import com.squareup.moshi.JsonClass;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

@JsonClass(generateAdapter = false)
public class VersionContext {

    private final Version version;
    private final boolean snapshot;
    private final Version previousVersion;
    private final ScmPosition position;
    private final boolean pinned;

    public VersionContext(Version version, boolean snapshot, Version previousVersion, ScmPosition position, boolean pinned) {
        this.version = version;
        this.snapshot = snapshot;
        this.previousVersion = previousVersion;
        this.position = position;
        this.pinned = pinned;
    }

    public VersionContext(Version version, boolean snapshot, Version previousVersion, ScmPosition position) {
        this(version, snapshot, previousVersion, position, false);
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

    public final boolean isPinned() {
        return pinned;
    }

    public VersionContext withPinned(boolean pinned) {
        return new VersionContext(version, snapshot, previousVersion, position, pinned);
    }

    @Override
    public String toString() {
        return "VersionContext{" +
            "version=" + version +
            ", snapshot=" + snapshot +
            ", previousVersion=" + previousVersion +
            ", position=" + position +
            ", pinned" + pinned +
            '}';
    }
}
