package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class TagNameSerializerTest extends Specification {

    def "default serializer should return concatenated prefix, version separator and version by default"() {
        given:
        TagProperties properties = tagProperties().build()

        expect:
        TagNameSerializer.DEFAULT.serializer.apply(properties, '0.1.0') == fullPrefix() + '0.1.0'
    }

    def "default deserializer should read version by stripping off prefix and version separator"() {
        given:
        TagProperties properties = tagProperties().build()

        expect:
        TagNameSerializer.DEFAULT.deserializer.apply(properties, scmPosition('master'), fullPrefix() + '0.1.0') == '0.1.0'
    }

    def "default serializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties properties = tagProperties().withPrefix('').build()

        expect:
        TagNameSerializer.DEFAULT.serializer.apply(properties, '0.1.0') == '0.1.0'
    }

    def "default deserializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties properties = tagProperties().withPrefix('').build()

        expect:
        TagNameSerializer.DEFAULT.deserializer.apply(properties, scmPosition('master'), '0.1.0') == '0.1.0'
    }
}
