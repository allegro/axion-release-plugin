package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties

class ScmPropertiesFactory {
    static ScmProperties create(VersionConfig config) {
        return new ScmProperties(
            config.repository.type.get(),
            config.repository.directory.asFile.get(),
            config.repository.remote.get(),
            config.repository.pushTagsOnly().get(),
            config.repository.fetchTags().get(),
            config.repository.attachRemote().isPresent(),
            config.repository.attachRemote().getOrNull(),
            config.repository.overriddenBranch().getOrNull(),
            ScmIdentityFactory.create(config.repository, config.repository.disableSshAgent().get())
        )
    }
}
