package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import spock.lang.Specification

class LocalOnlyResolverFactoryTest extends Specification {

    def "should resolve to localOnly when project release.localOnly property present"() {
        given:
        VersionConfig config = Fixtures.versionConfig(Fixtures.project(['release.localOnly' : '']))
        LocalOnlyResolver resolver = LocalOnlyResolverFactory.create(config)

        expect:
        config.localOnly().get()
        resolver.localOnly(false)
    }

    def "should resolve to localOnly when config has localOnly flag set"() {
        given:
        VersionConfig config = Fixtures.versionConfig()
        config.localOnly.set(true)
        LocalOnlyResolver resolver = LocalOnlyResolverFactory.create(config)

        expect:
        config.localOnly().get()
        resolver.localOnly(false)
    }
}
