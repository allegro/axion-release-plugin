package pl.allegro.tech.build.axion.release.factory

import pl.allegro.tech.build.axion.release.config.HooksConfig
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.HooksProperties

class HooksPropertiesFactory {

    static HooksProperties create(VersionConfig versionConfig, HooksConfig config) {
        if(versionConfig.createReleaseCommit) {
            versionConfig.hooks.pre('commit', versionConfig.releaseCommitMessage)
        }

        return new HooksProperties(preReleaseHooks: config.preReleaseHooks, postReleaseHooks: config.postReleaseHooks)
    }

}
