package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.util.FileLoader

class ScmIdentityFactory {

    static ScmIdentity create(RepositoryConfig config, boolean disableSshAgent) {
        if (config.customKey.isPresent()) {
            return ScmIdentity.keyIdentity(config.customKey.get(), config.customKeyPassword.get())
        }

        if (config.customKeyFile.isPresent()) {
            return ScmIdentity.keyIdentity(FileLoader.readFrom(config.customKeyFile.get().asFile), config.customKeyPassword.get())
        }

        if (config.customUsername.isPresent()) {
            return ScmIdentity.usernameIdentity(config.customUsername.get(), config.customPassword.get())
        }

        if (disableSshAgent) {
            return ScmIdentity.defaultIdentityWithoutAgents()
        }

        return ScmIdentity.defaultIdentity()
    }
}
