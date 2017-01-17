package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionContext {

    final Version version

    final boolean snapshot

    final Version previousVersion
    
    final ScmPosition position

    VersionContext(Version version, boolean snapshot, Version previousVersion, ScmPosition position) {
        this.version = version
        this.snapshot = snapshot
        this.previousVersion = previousVersion
        this.position = position
    }
}
