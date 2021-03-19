package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.PinProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VersionService {

    private static final Logger logger = Logging.getLogger(VersionService.class);
    private final VersionResolver versionResolver;
    private final VersionSanitizer sanitizer;

    public VersionService(VersionResolver versionResolver) {
        this.versionResolver = versionResolver;
        this.sanitizer = new VersionSanitizer();
    }

    public DecoratedVersion pinCurrentVersion(VersionProperties versionProperties, TagProperties tagRules, NextVersionProperties nextVersionRules,
        PinProperties pinning) {
        VersionContext versionContext = versionResolver.resolveVersion(versionProperties, tagRules, nextVersionRules);
        versionContext = versionContext.withPinned(true);
        writeVersionToPin(pinning.getPinFile(), versionContext);
        return decorateVersion(versionContext, versionProperties);
    }

    public VersionContext currentVersion(VersionProperties versionProperties, TagProperties tagRules, NextVersionProperties nextVersionRules,
        PinProperties pinning) {
        return getVersionFromPinOrResolveFrom(versionProperties, tagRules, nextVersionRules, pinning);
    }

    public DecoratedVersion currentDecoratedVersion(VersionProperties versionProperties, TagProperties tagRules,
        NextVersionProperties nextVersionRules, PinProperties pinning) {
        VersionContext versionContext = getVersionFromPinOrResolveFrom(versionProperties, tagRules, nextVersionRules, pinning);
        return decorateVersion(versionContext, versionProperties);
    }

    private DecoratedVersion decorateVersion(VersionContext versionContext, VersionProperties versionProperties) {
        String version = versionProperties.getVersionCreator().apply(versionContext.getVersion().toString(), versionContext.getPosition());

        if (versionProperties.isSanitizeVersion()) {
            version = sanitizer.sanitize(version);
        }

        String finalVersion = version;
        if (versionContext.isSnapshot() && !versionContext.isPinned()) {
            finalVersion = finalVersion + versionProperties.getSnapshotCreator().apply(version, versionContext.getPosition());
        }

        return new DecoratedVersion(versionContext.getVersion().toString(), finalVersion, versionContext.getPosition(),
            versionContext.getPreviousVersion().toString());
    }

    private VersionContext getVersionFromPinOrResolveFrom(VersionProperties versionRules, TagProperties tagRules,
        NextVersionProperties nextVersionRules, PinProperties pinning) {
        VersionContext version = null;
        if (pinning.isEnabled()) {
            version = readVersionFromPin(pinning.getPinFile());
        }
        if (version == null) {
            version = versionResolver.resolveVersion(versionRules, tagRules, nextVersionRules);
        }
        return version;
    }

    private VersionContext readVersionFromPin(File pinFile) {
        if (!pinFile.isFile()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(pinFile.toPath());
            return VersionContextSerializer.fromJson(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("Cannot read / deserialize version from pinfile, falling back to resolved version");
            return null;
        }
    }

    private void writeVersionToPin(File pinFile, VersionContext version) {
        try {
            String json = VersionContextSerializer.toJson(version);
            Files.write(pinFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Cannot serialize / write version to pinfile", e);
        }
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

    static class VersionContextSerializer {
        private static JsonAdapter<VersionContext> jsonAdapter;

        public static String toJson(VersionContext version) {
            return jsonAdapter().toJson(version);
        }

        public static VersionContext fromJson(String json) {
            try {
                return jsonAdapter().fromJson(json);
            } catch (IOException e) {
                return null;
            }
        }

        private static JsonAdapter<VersionContext> jsonAdapter() {
            if (jsonAdapter == null) {
                Moshi moshi = new Moshi.Builder().add(new VersionAdapter()).build();
                jsonAdapter = moshi.adapter(VersionContext.class);
            }
            return jsonAdapter;
        }
    }

    static class VersionAdapter {
        @ToJson
        String toJson(Version version) {
            return version.toString();
        }

        @FromJson
        Version fromJson(String version) {
            return Version.valueOf(version);
        }
    }
}
