package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

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

    DecoratedVersion currentDecoratedVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        VersionContext versionContext = versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        String version = versionRules.versionCreator(versionContext.version.toString(), versionContext.position)

        if (versionRules.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        String finalVersion = version
        if (versionContext.snapshot) {
            finalVersion = finalVersion + '-' + SNAPSHOT
        }

        return new DecoratedVersion(versionContext.version.toString(), finalVersion, versionContext.position)
    }

    static class DecoratedVersion {
        final String undecoratedVersion
        final String decoratedVersion
        final ScmPosition position

        DecoratedVersion(String undecoratedVersion, String decoratedVersion, ScmPosition position) {
            this.undecoratedVersion = undecoratedVersion
            this.decoratedVersion = decoratedVersion
            this.position = position
        }
    }
}
