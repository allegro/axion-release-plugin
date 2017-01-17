package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
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
        return Version.valueOf(tagProperties.deserialize(tagProperties, scmPosition, tagWithoutNextVersion))
    }

    Version initialVersion() {
        return Version.valueOf(tagProperties.initialVersion(tagProperties, scmPosition))
    }

    Map createFinalVersion(ScmState scmState, Version version) {
        boolean hasUncommittedChanges = !versionProperties.ignoreUncommittedChanges && scmState.hasUncommittedChanges
        boolean hasCommittedChanges = !scmState.onReleaseTag
        boolean hasChanges = hasCommittedChanges || hasUncommittedChanges

        boolean isSnapshot = versionProperties.forcedVersion || versionProperties.forceSnapshot || hasChanges || scmState.onNextVersionTag || scmState.noReleaseTagsFound
        boolean incrementVersion = versionProperties.forceSnapshot || (!scmState.onNextVersionTag && !scmState.noReleaseTagsFound && hasChanges)

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
}
