package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class TagNameSerializerTest extends Specification {

    def "default serializer should return concatenated prefix, version separator and version by default"() {
        given:
        TagProperties rules = new TagProperties(prefix: 'release', versionSeparator: '-')

        expect:
        TagNameSerializer.DEFAULT.serializer(rules, '0.1.0') == 'release-0.1.0'
    }

    def "default deserializer should read version by stripping off prefix and version separator"() {
        given:
        TagProperties rules = new TagProperties(prefix: 'release', versionSeparator: '-')

        expect:
        TagNameSerializer.DEFAULT.deserializer(rules, scmPosition('master'), 'release-0.1.0') == '0.1.0'
    }

    def "default serializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties rules = new TagProperties(prefix: '', versionSeparator: '-')

        expect:
        TagNameSerializer.DEFAULT.serializer(rules, '0.1.0') == '0.1.0'
    }

    def "default deserializer should use empty version separator when prefix is empty"() {
        given:
        TagProperties rules = new TagProperties(prefix: '', versionSeparator: '-')

        expect:
        TagNameSerializer.DEFAULT.deserializer(rules, scmPosition('master'), '0.1.0') == '0.1.0'
    }
}
