package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig

class ScmIdentityResolver {

    static ScmIdentity resolve(RepositoryConfig config) {
        ScmIdentity identity
        if (config.customKey) {
            identity = ScmIdentity.customIdentity(resolveKey(config.customKey), config.customKeyPassword)
        } else {
            identity = ScmIdentity.defaultIdentity()
        }
        return identity
    }

    private static String resolveKey(def key) {
        switch(key.class) {
            case File:
                return key.getText('UTF-8')
            default:
                return key.toString()
        }
    }
}
