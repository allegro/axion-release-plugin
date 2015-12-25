package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties

class NextVersionSerializerTest extends Specification {

    NextVersionProperties rules = nextVersionProperties().withSuffix('beta').build()

    def "default serializer should append separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.serializer(rules, 'release-1.0.0')
        
        then:
        version == 'release-1.0.0-beta'
    }
    
    def "default deserializer should trim separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.deserializer(rules, ScmPosition.onTag('release-1.0.0-beta'))

        then:
        version == 'release-1.0.0'
    }
}
