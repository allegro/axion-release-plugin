package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class NextVersionSerializerTest extends Specification {

    NextVersionProperties rules = nextVersionProperties().withSuffix('beta').build()

    def "default serializer should append separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.serializer(rules, 'v1.0.0')

        then:
        version == 'v1.0.0-beta'
    }

    def "default deserializer should trim separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.deserializer(rules, scmPosition('master'), 'v1.0.0-beta')

        then:
        version == 'v1.0.0'
    }
}
