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

    VersionContext currentVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        return versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules)
    }

    String currentDecoratedVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        VersionContext versionContext = versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        String version = versionRules.versionCreator(versionContext.version.toString(), versionContext.position)

        if (versionRules.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        if (versionContext.snapshot) {
            version = version + '-' + SNAPSHOT
        }

        return version
    }
}
