package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.util.FileLoader

class ScmIdentityResolver {

    static ScmIdentity resolve(RepositoryConfig config) {
        ScmIdentity identity
        if (config.customKey) {
            identity = ScmIdentity.customIdentity(FileLoader.readIfFile(config.customKey), config.customKeyPassword)
        } else {
            identity = ScmIdentity.defaultIdentity()
        }
        return identity
    }
}
