package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionWithPosition {

    final Version version

    final Version previousVersion
    
    final ScmPosition position

    boolean isSnapshotVersion = false

    VersionWithPosition(Version version, Version previousVersion, ScmPosition position) {
        this.version = version
        this.previousVersion = previousVersion
        this.position = position
    }

    static VersionWithPosition withoutPosition(Version version) {
        return new VersionWithPosition(version, null, null)
    }

    boolean forcedVersion() {
        return position == null
    }

    void setSnapshotVersion(boolean isSnapshotVersion) {
        this.isSnapshotVersion = isSnapshotVersion
    }
}
