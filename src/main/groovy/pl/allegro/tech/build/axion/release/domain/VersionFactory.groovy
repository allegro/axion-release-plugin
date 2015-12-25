package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionFactory {

    Version create(ScmPositionContext context,
                   VersionProperties versionRules,
                   TagProperties tagRules,
                   NextVersionProperties nextVersionRules) {
        Version version
        
        if (versionRules.forcedVersion) {
            version = Version.valueOf(versionRules.forcedVersion)
        } else {
            if (context.position.tagless()) {
                version = Version.valueOf(initialVersion(tagRules.initialVersion, tagRules, context.position))
            } else {
                version = Version.valueOf(readVersionFromPosition(context, tagRules, nextVersionRules))
                
                boolean hasUncommittedChanges = !versionRules.ignoreUncommittedChanges && context.position.hasUncommittedChanges
                boolean hasChanges = !context.position.onTag || hasUncommittedChanges || versionRules.forceSnapshot
                
                if (hasChanges && !context.nextVersionTag) {
                    version = versionRules.versionIncrementer(new VersionIncrementerContext(version, context.position))
                }
            }
        }
        return version
    }

    private String initialVersion(Closure toCall, TagProperties tagRules, ScmPosition currentPosition) {
        return toCall(tagRules, currentPosition)
    }

    private String readVersionFromPosition(ScmPositionContext context, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        String tagWithoutNextVersion = context.position.latestTag
        if(context.nextVersionTag) {
            tagWithoutNextVersion = nextVersionRules.deserializer(nextVersionRules, context.position)
        }
        return tagRules.deserialize(tagRules, context.position, tagWithoutNextVersion)
    }
}
