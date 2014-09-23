package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionWithPosition {

    final Version version

    final ScmPosition position

    VersionWithPosition(Version version, ScmPosition position) {
        this.version = version
        this.position = position
    }

    static VersionWithPosition withoutPosition(Version version) {
        return new VersionWithPosition(version, null)
    }

    boolean forcedVersion() {
        return position == null
    }
}
