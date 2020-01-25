package pl.allegro.tech.build.axion.release.domain;

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionService {

    public static final String SNAPSHOT = "SNAPSHOT";
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
        String version = versionProperties.getVersionCreator().call(versionContext.getVersion().toString(), versionContext.getPosition());

        if (versionProperties.isSanitizeVersion()) {
            version = sanitizer.sanitize(version);
        }


        String finalVersion = version;
        if (versionContext.isSnapshot()) {
            finalVersion = finalVersion + "-" + SNAPSHOT;
        }


        return new DecoratedVersion(versionContext.getVersion().toString(), finalVersion, versionContext.getPosition());
    }

    public static class DecoratedVersion {

        private final String undecoratedVersion;
        private final String decoratedVersion;
        private final ScmPosition position;

        public DecoratedVersion(String undecoratedVersion, String decoratedVersion, ScmPosition position) {
            this.undecoratedVersion = undecoratedVersion;
            this.decoratedVersion = decoratedVersion;
            this.position = position;
        }

        public final String getUndecoratedVersion() {
            return undecoratedVersion;
        }

        public final String getDecoratedVersion() {
            return decoratedVersion;
        }

        public final ScmPosition getPosition() {
            return position;
        }
    }
}
