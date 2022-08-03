package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import spock.lang.Specification

class ScmPropertiesFactoryTest extends Specification {

    private VersionConfig config = Fixtures.versionConfig()

    def "should return remote name and tagsOnly option from configuration when no flags on project"() {
        given:
        config.repository.remote.set( 'someRemote')
        config.repository.pushTagsOnly.set(true)

        when:
        ScmProperties properties = ScmPropertiesFactory.create(config)

        then:
        properties.remote == 'someRemote'
        properties.pushTagsOnly
    }

    def "should return true for tagsOnly when enabled via project flag"() {
        given:
        def project = Fixtures.project(['release.pushTagsOnly':''])
        VersionConfig config = Fixtures.versionConfig(project)
        config.repository.pushTagsOnly.set(false)

        when:
        ScmProperties properties = ScmPropertiesFactory.create(config)

        then:
        properties.pushTagsOnly
    }
}
