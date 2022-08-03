package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.NextVersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder
import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties.*

class NextVersionPropertiesFactoryTest extends Specification {

    def "should copy non-project properties from NextVersionConfig object"() {
        given:
        NextVersionConfig config = Fixtures.nextVersionConfig(Fixtures.project(['release.version': '1.0.0']))

        config.serializer.set(  (Serializer) { NextVersionProperties config_, String version -> 'serialize'})
        config.deserializer.set( (Deserializer)  { NextVersionProperties config_, ScmPosition position, String tagName -> 'deserialize'})
        config.suffix.set( 'something')
        config.separator.set('=')

        when:
        NextVersionProperties properties = config.nextVersionProperties()

        then:
        properties.serializer.apply(properties, "any") == 'serialize'
        properties.deserializer.apply(properties, new ScmPosition("shortsha", "longsha", "master"), "any") == 'deserialize'
        properties.suffix == 'something'
        properties.separator == '='
    }

    def "should read nextVersion from 'release.version' property"() {
        given:
        NextVersionConfig config = Fixtures.nextVersionConfig(Fixtures.project(['release.version': '1.0.0']))

        when:
        NextVersionProperties properties = config.nextVersionProperties()

        then:
        properties.nextVersion
        properties.nextVersion == '1.0.0'
    }

    def "should read nextVersion from deprecated 'release.nextVersion' property"() {
        given:
        NextVersionConfig config = Fixtures.nextVersionConfig(Fixtures.project(['release.nextVersion': '1.0.0']))

        when:
        NextVersionProperties properties = config.nextVersionProperties()

        then:
        properties.nextVersion
        properties.nextVersion == '1.0.0'
    }
}
