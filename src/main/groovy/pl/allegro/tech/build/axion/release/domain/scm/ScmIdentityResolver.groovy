package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig

class ScmIdentityResolver {

    static ScmIdentity resolve(RepositoryConfig config) {
        ScmIdentity identity
        if (config.customKey) {
            identity = ScmIdentity.customIdentity(config.customKey, config.customKeyPassword)
        } else {
            identity = ScmIdentity.defaultIdentity()
        }
        return identity
    }
}
