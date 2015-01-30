package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

import java.util.regex.Pattern

class VersionResolver {

    private final ScmRepository repository

    VersionResolver(ScmRepository repository) {
        this.repository = repository
    }

    VersionWithPosition resolveVersion(VersionConfig versionConfig, VersionReadOptions options) {
        Version version

        Pattern pattern = Pattern.compile("^${versionConfig.tag.prefix}.*(|${versionConfig.nextVersion.suffix})\$")
        ScmPosition position = repository.currentPosition(pattern)
        boolean afterNextVersionTag = afterNextVersionTag(position, versionConfig.nextVersion)
        
        if(afterNextVersionTag) {
            position = ScmPosition.notOnTag(position)
        }
        
        if (options.forcedVersion) {
            version = Version.valueOf(options.forcedVersion)
        } else {
            if (position.tagless()) {
                version = Version.valueOf(initialVersion(versionConfig, position))
            } else {
                version = Version.valueOf(readVersionFromPosition(position, versionConfig, afterNextVersionTag))
                if (!position.onTag && !afterNextVersionTag) {
                    version = version.incrementPatchVersion()
                }
            }
        }

        return new VersionWithPosition(version, position)
    }

    private String initialVersion(VersionConfig serializationConfig, ScmPosition currentPosition) {
        return serializationConfig.tag.initialVersion(serializationConfig.tag, currentPosition)
    }

    private String readVersionFromPosition(ScmPosition position, VersionConfig config, nextVersionTag) {
        String tagWithoutNextVersion = position.latestTag
        if(nextVersionTag) {
            tagWithoutNextVersion = config.nextVersion.deserializer(config.nextVersion, position)
        }
        return config.tag.deserialize(config.tag, position, tagWithoutNextVersion)
    }
    
    private boolean afterNextVersionTag(ScmPosition position, NextVersionConfig config) {
        return position.latestTag?.endsWith(config.suffix)
    }
}
