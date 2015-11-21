package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionFactory {

    Version create(ScmPositionContext context,
                   VersionConfig config,
                   VersionReadOptions options,
                   TagNameSerializationRules tagConfig) {
        Version version
        
        if (options.forcedVersion) {
            version = Version.valueOf(options.forcedVersion)
        } else {
            if (context.position.tagless()) {
                version = Version.valueOf(initialVersion(config.tag.initialVersion, tagConfig, context.position))
            } else {
                version = Version.valueOf(readVersionFromPosition(context, config, tagConfig))
                
                boolean hasUncommitedChanges = !options.ignoreUncommittedChanges && context.position.hasUncommittedChanges
                boolean hasChanges = !context.position.onTag || hasUncommitedChanges || options.forceSnapshot
                
                if (hasChanges && !context.nextVersionTag) {
                    version = config.versionIncrementer(new VersionIncrementerContext(version, context.position))
                }
            }
        }
        return version
    }

    private String initialVersion(Closure toCall, TagNameSerializationRules config, ScmPosition currentPosition) {
        return toCall(config, currentPosition)
    }

    private String readVersionFromPosition(ScmPositionContext context, VersionConfig config, TagNameSerializationRules tagConfig) {
        String tagWithoutNextVersion = context.position.latestTag
        if(context.nextVersionTag) {
            tagWithoutNextVersion = config.nextVersion.deserializer(config.nextVersion, context.position)
        }
        return config.tag.deserialize(tagConfig, context.position, tagWithoutNextVersion)
    }
}
