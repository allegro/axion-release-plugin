package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import com.github.zafarkhaja.semver.Version

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class VersionContextBuilder {
    private Version version = Version.parse('1.0.0')

    private boolean snapshot = false

    private Version previousVersion = Version.parse('1.0.0')

    private ScmPosition position = scmPosition('master')

    private VersionContextBuilder() {
    }

    static VersionContextBuilder scmPosition() {
        return new VersionContextBuilder()
    }

    static VersionContext versionContext(ScmPosition position, boolean snapshot) {
        return new VersionContextBuilder().withPosition(position).withSnapshot(snapshot).build()
    }

    VersionContext build() {
        return new VersionContext(version, snapshot, previousVersion, position)
    }

    VersionContextBuilder withVersion(Version version) {
        this.version = version
        return this
    }

    VersionContextBuilder withSnapshot(boolean snapshot) {
        this.snapshot = snapshot
        return this
    }

    VersionContextBuilder withPreviousVersion(Version previousVersion) {
        this.previousVersion = previousVersion
        return this
    }

    VersionContextBuilder withPosition(ScmPosition position) {
        this.position = position
        return this
    }


}
