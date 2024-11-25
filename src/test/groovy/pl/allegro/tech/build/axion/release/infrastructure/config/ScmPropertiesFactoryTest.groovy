package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import spock.lang.Specification

class ScmPropertiesFactoryTest extends Specification {

    private VersionConfig config = Fixtures.versionConfig()

    def "should return remote name and tagsOnly option from configuration when no flags on project"() {
        given:
        config.repository.remote.set('someRemote')
        config.repository.pushTagsOnly.set(true)

        when:
        ScmProperties properties = ScmPropertiesFactory.create(config)

        then:
        properties.remote == 'someRemote'
        properties.pushTagsOnly
    }
}
