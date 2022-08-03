package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionService {

    private final VersionResolver versionResolver;
    private final VersionSanitizer sanitizer;


    public VersionService(VersionResolver versionResolver) {
        this.versionResolver = versionResolver;
        this.sanitizer = new VersionSanitizer();
    }

    public VersionContext currentVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        return versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules);
    }

    public DecoratedVersion currentDecoratedVersion(VersionProperties versionProperties, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        VersionContext versionContext = versionResolver.resolveVersion(versionProperties, tagRules, nextVersionRules);
        String version = versionProperties.getVersionCreator().apply(versionContext.getVersion().toString(), versionContext.getPosition());

        if (versionProperties.isSanitizeVersion()) {
            version = sanitizer.sanitize(version);
        }


        String finalVersion = version;
        if (versionContext.isSnapshot()) {
            finalVersion = finalVersion + versionProperties.getSnapshotCreator().apply(version,  versionContext.getPosition());
        }

        return new DecoratedVersion(versionContext.getVersion().toString(), finalVersion, versionContext.getPosition(),
            versionContext.getPreviousVersion().toString());
    }

    public static class DecoratedVersion {
        private final String undecoratedVersion;
        private final String decoratedVersion;
        private final ScmPosition position;
        private final String previousVersion;

        public DecoratedVersion(String undecoratedVersion, String decoratedVersion, ScmPosition position,
                                String previousVersion) {
            this.undecoratedVersion = undecoratedVersion;
            this.decoratedVersion = decoratedVersion;
            this.position = position;
            this.previousVersion = previousVersion;
        }

        @Input
        public final String getUndecoratedVersion() {
            return undecoratedVersion;
        }

        @Input
        public final String getDecoratedVersion() {
            return decoratedVersion;
        }

        @Nested
        public final ScmPosition getPosition() {
            return position;
        }

        @Input
        public final String getPreviousVersion() {
            return previousVersion;
        }
    }
}
