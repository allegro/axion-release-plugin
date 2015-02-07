package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class VersionFactory {

    Version create(ScmPositionContext context, VersionConfig config, VersionReadOptions options) {
        Version version
        
        if (options.forcedVersion) {
            version = Version.valueOf(options.forcedVersion)
        } else {
            if (context.position.tagless()) {
                version = Version.valueOf(initialVersion(config.tag, context.position))
            } else {
                version = Version.valueOf(readVersionFromPosition(context, config))
                if (!context.position.onTag && !context.nextVersionTag) {
                    version = version.incrementPatchVersion()
                }
            }
        }
        return version
    }

    private String initialVersion(TagNameSerializationRules config, ScmPosition currentPosition) {
        return config.initialVersion(config, currentPosition)
    }

    private String readVersionFromPosition(ScmPositionContext context, VersionConfig config) {
        String tagWithoutNextVersion = context.position.latestTag
        if(context.nextVersionTag) {
            tagWithoutNextVersion = config.nextVersion.deserializer(config.nextVersion, context.position)
        }
        return config.tag.deserialize(config.tag, context.position, tagWithoutNextVersion)
    }
}
