package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class TagNameSerializerTest extends Specification {

    static final TagProperties.Serializer DEFAULT_SERIALIZER = TagNameSerializer.DEFAULT.serializer
    static final TagProperties.Deserializer DEFAULT_DESERIALIZER = TagNameSerializer.DEFAULT.deserializer
    static final ScmPosition MASTER = scmPosition('master')

    def "default serializer should return concatenated prefix, version separator and version by default"() {
        given:
        TagProperties properties = tagProperties().build()

        expect:
        DEFAULT_SERIALIZER.apply(properties, '0.1.0') == fullPrefix() + '0.1.0'
    }

    def "default deserializer should read version by stripping off prefix and version separator"() {
        given:
        TagProperties properties = tagProperties().build()

        expect:
        DEFAULT_DESERIALIZER.apply(properties, MASTER, fullPrefix() + '0.1.0') == '0.1.0'
    }

    def "default serializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties properties = tagProperties().withPrefix('').build()

        expect:
        DEFAULT_SERIALIZER.apply(properties, '0.1.0') == '0.1.0'
    }

    def "default deserializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties properties = tagProperties().withPrefix('').build()

        expect:
        DEFAULT_DESERIALIZER.apply(properties, MASTER, '0.1.0') == '0.1.0'
    }

    def "default deserializer should use fallback prefix in case tag doesn't match main prefix"() {
        given:
        TagProperties properties = tagProperties()
            .withPrefix("v")
            .withFallbackPrefixes(List.of('example-service-'))
            .build()

        when:
        String deserializedVersion = DEFAULT_DESERIALIZER.apply(properties, MASTER, 'example-service-0.1.0')

        then:
        deserializedVersion == '0.1.0'
    }

    def "default deserializer should use second fallback prefix in case tag doesn't match main and first fallback"() {
        given:
        TagProperties properties = tagProperties()
            .withPrefix("v")
            .withFallbackPrefixes(List.of('example-service-', 'release-'))
            .build()

        when:
        String deserializedVersion = DEFAULT_DESERIALIZER.apply(properties, MASTER, 'release-0.1.0')

        then:
        deserializedVersion == '0.1.0'
    }

    def "default deserializer should give main prefix precedence over fallback prefixes"() {
        given:
        TagProperties properties = tagProperties()
            .withPrefix("abc")
            .withFallbackPrefixes(List.of('a', 'ab'))
            .build()

        when:
        String deserializedVersion = DEFAULT_DESERIALIZER.apply(properties, MASTER, 'abc0.1.0')

        then:
        deserializedVersion == '0.1.0'
    }

    def "default deserializer should check whether tag matches prefix and separator"() {
        given:
        TagProperties properties = tagProperties()
            .withPrefix("v")
            .withSeparator("-")
            .build()

        when:
        String deserializedVersion = DEFAULT_DESERIALIZER.apply(properties, MASTER, 'v0.1.0')

        then:
        deserializedVersion == null
    }
}
