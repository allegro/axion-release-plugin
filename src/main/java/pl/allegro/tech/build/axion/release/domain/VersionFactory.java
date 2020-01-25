package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.regex.Pattern;

public class VersionFactory {

    private final VersionProperties versionProperties;
    private final TagProperties tagProperties;
    private final NextVersionProperties nextVersionProperties;
    private final ScmPosition position;

    public VersionFactory(
        VersionProperties versionProperties,
        TagProperties tagProperties,
        NextVersionProperties nextVersionProperties,
        ScmPosition position
    ) {
        this.tagProperties = tagProperties;
        this.nextVersionProperties = nextVersionProperties;
        this.versionProperties = versionProperties;
        this.position = position;
    }

    public Version versionFromTag(String tag) {
        String tagWithoutNextVersion = tag;
        if (Pattern.matches(".*" + nextVersionProperties.getSuffix() + "$", tag)) {
            tagWithoutNextVersion = nextVersionProperties.getDeserializer().call(nextVersionProperties, position, tag);
        }

        try {
            return Version.valueOf(
                tagProperties.getDeserialize().call(tagProperties, position, tagWithoutNextVersion)
            );
        } catch (ParseException parseException) {
            throw new TagParseException(tagProperties.getPrefix(), tagWithoutNextVersion, parseException);
        }

    }

    public Version initialVersion() {
        return Version.valueOf(tagProperties.getInitialVersion().call(tagProperties, position));
    }

    public FinalVersion createFinalVersion(ScmState scmState, Version version) {
        boolean hasUncommittedChanges = !versionProperties.isIgnoreUncommittedChanges() && scmState.hasUncommittedChanges();
        boolean hasCommittedChanges = !scmState.isOnReleaseTag();
        boolean hasChanges = hasCommittedChanges || hasUncommittedChanges;

        boolean forcesSameVersionAsCurrent = versionProperties.forceVersion() && versionProperties.getForcedVersion().equals(version.toString());
        boolean forceVersionShouldForceSnapshot = versionProperties.forceVersion() && !forcesSameVersionAsCurrent;

        boolean isSnapshot = forceVersionShouldForceSnapshot || versionProperties.isForceSnapshot() || hasChanges || scmState.isOnNextVersionTag() || scmState.isNoReleaseTagsFound();
        boolean proposedVersionIsAlreadySnapshot = scmState.isOnNextVersionTag() || scmState.isNoReleaseTagsFound();
        boolean incrementVersion = ((versionProperties.isForceSnapshot() || hasChanges) && !proposedVersionIsAlreadySnapshot);

        Version finalVersion = version;
        if (StringGroovyMethods.asBoolean(versionProperties.getForcedVersion())) {
            finalVersion = Version.valueOf(versionProperties.getForcedVersion());
        } else if (incrementVersion) {
            finalVersion = versionProperties.getVersionIncrementer().call(new VersionIncrementerContext(
                version, position
            ));
        }

        return new FinalVersion(
            finalVersion,
            isSnapshot
        );
    }

    static class FinalVersion {
        final Version version;
        final boolean snapshot;

        private FinalVersion(Version version, boolean snapshot) {
            this.version = version;
            this.snapshot = snapshot;
        }
    }

    public static class TagParseException extends RuntimeException {
        public TagParseException(String prefix, String parsedText, final ParseException cause) {
            super("Failed to parse version: " + parsedText + " that matched configured prefix: " + prefix + ". There can be no tags that match the prefix but contain non-SemVer string after the prefix. Detailed message: " + cause.toString());
        }
    }
}
