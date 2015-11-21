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

    VersionWithPosition currentVersion(VersionConfig versionConfig, VersionReadOptions options, TagNameSerializationRules tagConfig) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options, tagConfig)

        if(isSnapshotVersion(positionedVersion, options)) {
            positionedVersion.asSnapshotVersion()
        }

        return positionedVersion
    }

    String currentDecoratedVersion(VersionConfig versionConfig, VersionReadOptions options, TagNameSerializationRules tagConfig) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options, tagConfig)
        String version = versionDecorator.createVersion(versionConfig, positionedVersion)

        if (versionConfig.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        if(isSnapshotVersion(positionedVersion, options)) {
            version = version + '-' + SNAPSHOT
        }

        return version
    }
    
    private boolean isSnapshotVersion(VersionWithPosition positionedVersion, VersionReadOptions options) {
        boolean hasUncommittedChanges = !options.ignoreUncommittedChanges && positionedVersion.position.hasUncommittedChanges
        return !positionedVersion.position.onTag || hasUncommittedChanges || options.forceSnapshot
    }
}
