package pl.allegro.tech.build.axion.release.domain

class VersionService {

    static final String SNAPSHOT = "SNAPSHOT"

    private final VersionDecorator versionDecorator

    private final VersionResolver versionResolver

    private final VersionSanitizer sanitizer

    VersionService(VersionResolver versionResolver) {
        this.versionResolver = versionResolver
        this.versionDecorator = new VersionDecorator()
        this.sanitizer = new VersionSanitizer()
    }

    VersionWithPosition currentVersion(VersionConfig versionConfig, VersionReadOptions options) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options)

        if (!positionedVersion.position.onTag) {
            positionedVersion.setSnapshotVersion(true)
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
