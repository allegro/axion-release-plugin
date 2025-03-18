package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class ProjectVersion {

    final Version version

    final ScmPosition position

    final String decoratedVersion

    ProjectVersion(Version version, ScmPosition position, String decoratedVersion) {
        this.version = version
        this.position = position
        this.decoratedVersion = decoratedVersion
    }
}
