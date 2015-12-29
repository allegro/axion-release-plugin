package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.util.FileLoader

class ScmIdentityFactory {

    static ScmIdentity create(RepositoryConfig config) {
        ScmIdentity identity
        if (config.customKey) {
            identity = ScmIdentity.keyIdentity(FileLoader.readIfFile(config.customKey), config.customKeyPassword)
        } else if (config.customUsername) {
            identity = ScmIdentity.usernameIdentity(config.customUsername, config.customPassword)
        } else {
            identity = ScmIdentity.defaultIdentity()
        }
        return identity
    }
}
