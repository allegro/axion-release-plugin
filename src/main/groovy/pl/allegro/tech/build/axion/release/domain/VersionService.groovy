package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class VersionService {

    static final String SNAPSHOT = "SNAPSHOT"

    private final ScmRepository repository

    private final VersionDecorator versionDecorator

    private final VersionResolver versionResolver

    private final VersionSanitizer sanitizer

    VersionService(VersionResolver versionResolver) {
        this.repository = repository
        this.versionResolver = versionResolver
        this.versionDecorator = new VersionDecorator()
        this.sanitizer = new VersionSanitizer()
    }

    VersionWithPosition currentVersion(VersionConfig versionConfig, VersionReadOptions options) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options)

        if (!positionedVersion.position.onTag) {
            positionedVersion = new VersionWithPosition(
                    positionedVersion.version.setPreReleaseVersion(SNAPSHOT),
                    positionedVersion.position
            )
        }

        return positionedVersion
    }

    String currentDecoratedVersion(VersionConfig versionConfig, VersionReadOptions options) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options)
        String version = versionDecorator.createVersion(versionConfig, positionedVersion)

        if (versionConfig.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        if (!positionedVersion.position.onTag) {
            version = version + '-' + SNAPSHOT
        }

        return version
    }
}
