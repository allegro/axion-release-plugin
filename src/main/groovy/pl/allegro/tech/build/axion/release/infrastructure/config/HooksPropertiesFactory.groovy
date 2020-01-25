package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.properties.HooksProperties

class HooksPropertiesFactory {

    static HooksProperties create(VersionConfig versionConfig, HooksConfig config) {
        if(versionConfig.createReleaseCommit) {
            versionConfig.hooks.pre('commit', versionConfig.releaseCommitMessage)
        }

        return new HooksProperties(config.preReleaseHooks, config.postReleaseHooks)
    }

}
