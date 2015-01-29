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
        boolean nextVersionTag = nextVersionTag(position, versionConfig.nextVersion)
        
        if(nextVersionTag) {
            position = ScmPosition.notOnTag(position)
        }
        
        if (options.forcedVersion) {
            version = Version.valueOf(options.forcedVersion)
        } else {
            if (position.tagless()) {
                version = Version.valueOf(initialVersion(versionConfig, position))
            } else {
                version = Version.valueOf(readVersionFromPosition(position, versionConfig, nextVersionTag))
                if (!position.onTag && !nextVersionTag) {
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
    
    private boolean nextVersionTag(ScmPosition position, NextVersionConfig config) {
        return position.onTag && position.latestTag.endsWith(config.suffix)
    }
}
