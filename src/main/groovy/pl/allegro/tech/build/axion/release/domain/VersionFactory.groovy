package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.ParseException
import com.github.zafarkhaja.semver.Version
import org.gradle.api.GradleException
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionFactory {

    private final VersionProperties versionProperties

    private final TagProperties tagProperties

    private final NextVersionProperties nextVersionProperties

    private final ScmPosition scmPosition

    VersionFactory(VersionProperties versionProperties,
                   TagProperties tagProperties,
                   NextVersionProperties nextVersionProperties,
                   ScmPosition scmPosition) {
        this.tagProperties = tagProperties
        this.nextVersionProperties = nextVersionProperties
        this.scmPosition = scmPosition
        this.versionProperties = versionProperties
    }

    Version versionFromTag(String tag) {
        String tagWithoutNextVersion = tag
        if (tag ==~ /.*${nextVersionProperties.suffix}$/) {
            tagWithoutNextVersion = nextVersionProperties.deserializer(nextVersionProperties, scmPosition, tag)
        }
        try {
            return Version.valueOf(tagProperties.deserialize(tagProperties, scmPosition, tagWithoutNextVersion))
        } catch (ParseException parseException) {
            throw new TagParseException(tagProperties.prefix, tagWithoutNextVersion, parseException)
        }
    }

    Version initialVersion() {
        return Version.valueOf(tagProperties.initialVersion(tagProperties, scmPosition))
    }

    Map createFinalVersion(ScmState scmState, Version version) {
        boolean hasUncommittedChanges = !versionProperties.ignoreUncommittedChanges && scmState.hasUncommittedChanges
        boolean hasCommittedChanges = !scmState.onReleaseTag
        boolean hasChanges = hasCommittedChanges || hasUncommittedChanges

        boolean forcesSameVersionAsCurrent = versionProperties.forceVersion() &&
            versionProperties.forcedVersion == version.toString()
        boolean forceVersionShouldForceSnapshot = versionProperties.forceVersion() && !forcesSameVersionAsCurrent

        boolean isSnapshot = forceVersionShouldForceSnapshot || versionProperties.forceSnapshot || hasChanges || scmState.onNextVersionTag || scmState.noReleaseTagsFound
        boolean proposedVersionIsAlreadySnapshot = scmState.onNextVersionTag || scmState.noReleaseTagsFound
        boolean incrementVersion = ((versionProperties.forceSnapshot || hasChanges) && !proposedVersionIsAlreadySnapshot)

        Version finalVersion = version
        if (versionProperties.forcedVersion) {
            finalVersion = Version.valueOf(versionProperties.forcedVersion)
        } else if (incrementVersion) {
            finalVersion = versionProperties.versionIncrementer(new VersionIncrementerContext(version, scmPosition))
        }

        return [
            version : finalVersion,
            snapshot: isSnapshot
        ]
    }

    static class TagParseException extends RuntimeException {
        TagParseException(String prefix, String parsedText, ParseException cause) {
            super("Failed to parse version: $parsedText that matched configured prefix: $prefix. " +
                "There can be no tags that match the prefix but contain non-SemVer string after the prefix. " +
                "Detailed message: ${cause.toString()}")
        }
    }
}
