package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class VersionResolver {

    private final ScmRepository repository

    VersionResolver(ScmRepository repository) {
        this.repository = repository
    }

    VersionWithPosition resolveVersion(VersionConfig versionConfig, VersionReadOptions options) {
        Version version
        ScmPosition position = repository.currentPosition(versionConfig.tag.prefix)

        if (options.forcedVersion) {
            version = Version.valueOf(options.forcedVersion)
        } else {
            if (position.tagless()) {
                version = Version.valueOf(initialVersion(versionConfig, position))
            } else {
                version = Version.valueOf(readVersionFromPosition(position, versionConfig))
                if(!position.onTag) {
                    version = version.incrementPatchVersion()
                }
            }
        }

        return new VersionWithPosition(version, position)
    }

    private String initialVersion(VersionConfig serializationConfig, ScmPosition currentPosition) {
        return serializationConfig.tag.initialVersion(serializationConfig.tag, currentPosition)
    }

    private String readVersionFromPosition(ScmPosition position, VersionConfig serializationConfig) {
        return serializationConfig.tag.deserialize(serializationConfig.tag, position)
    }
}
