package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import spock.lang.Specification

class ScmIdentityResolverTest extends Specification {

    def "should return default identity when no key set"() {
        given:
        RepositoryConfig config = new RepositoryConfig()

        when:
        ScmIdentity identity = ScmIdentityResolver.resolve(config)

        then:
        identity.useDefault
    }

    def "should return custom identity when key set"() {
        given:
        RepositoryConfig config = new RepositoryConfig(customKey: 'key', customKeyPassword: 'password')

        when:
        ScmIdentity identity = ScmIdentityResolver.resolve(config)

        then:
        !identity.useDefault
        identity.privateKey == 'key'
        identity.passPhrase == 'password'
    }
}
