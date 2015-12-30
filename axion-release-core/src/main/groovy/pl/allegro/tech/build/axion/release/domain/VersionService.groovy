package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

class VersionService {

    public static final String SNAPSHOT = "SNAPSHOT"

    private final VersionResolver versionResolver

    private final VersionSanitizer sanitizer

    VersionService(VersionResolver versionResolver) {
        this.versionResolver = versionResolver
        this.sanitizer = new VersionSanitizer()
    }

    VersionWithPosition currentVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        if(isSnapshotVersion(positionedVersion, versionRules)) {
            positionedVersion.asSnapshotVersion()
        }

        return positionedVersion
    }

    String currentDecoratedVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        String version = versionRules.versionCreator(positionedVersion.version.toString(), positionedVersion.position)

        if (versionRules.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        if(isSnapshotVersion(positionedVersion, versionRules)) {
            version = version + '-' + SNAPSHOT
        }

        return version
    }
    
    private boolean isSnapshotVersion(VersionWithPosition positionedVersion, VersionProperties versionRules) {
        boolean hasUncommittedChanges = !versionRules.ignoreUncommittedChanges && positionedVersion.position.hasUncommittedChanges
        return !positionedVersion.position.onTag || hasUncommittedChanges || versionRules.forceSnapshot
    }
}
