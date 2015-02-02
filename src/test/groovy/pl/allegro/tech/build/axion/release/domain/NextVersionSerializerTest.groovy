package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class NextVersionSerializerTest extends Specification {

    NextVersionConfig config = new NextVersionConfig(suffix: 'beta', separator: '-')
    
    def "default serializer should append separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.serializer(config, 'release-1.0.0')
        
        then:
        version == 'release-1.0.0-beta'
    }
    
    def "default deserializer should trim separator and suffix"() {
        when:
        String version = NextVersionSerializer.DEFAULT.deserializer(config, ScmPosition.onTag('release-1.0.0-beta'))

        then:
        version == 'release-1.0.0'
    }
}
