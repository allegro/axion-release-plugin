package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionWithPosition {

    final Version version

    final Version lastReleasedVersion
    
    final ScmPosition position

    VersionWithPosition(Version version, Version lastReleasedVersion, ScmPosition position) {
        this.version = version
        this.lastReleasedVersion = lastReleasedVersion
        this.position = position
    }

    static VersionWithPosition withoutPosition(Version version, Version lastReleasedVersion) {
        return new VersionWithPosition(version, null, null)
    }

    boolean forcedVersion() {
        return position == null
    }
}
