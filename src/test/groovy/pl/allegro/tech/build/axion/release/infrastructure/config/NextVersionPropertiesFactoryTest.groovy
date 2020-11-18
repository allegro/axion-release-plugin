package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.NextVersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class NextVersionPropertiesFactoryTest extends Specification {

    private Project project

    private NextVersionConfig config = new NextVersionConfig()

    def setup() {
        project = builder().build()
    }

    def "should copy non-project properties from NextVersionConfig object"() {
        given:
        project.extensions.extraProperties.set('release.version', '1.0.0')

        config.serializer = { config, version -> 'serialize'}
        config.deserializer = { config, position, tagName -> 'deserialize'}
        config.suffix = 'something'
        config.separator = '='

        when:
        NextVersionProperties properties = NextVersionPropertiesFactory.create(project, config)

        then:
        properties.serializer.apply(properties, "any") == 'serialize'
        properties.deserializer.apply(properties, new ScmPosition("shortsha", "longsha", "master"), "any") == 'deserialize'
        properties.suffix == 'something'
        properties.separator == '='
    }

    def "should read nextVersion from 'release.version' property"() {
        given:
        project.extensions.extraProperties.set('release.version', '1.0.0')

        when:
        NextVersionProperties properties = NextVersionPropertiesFactory.create(project, config)

        then:
        properties.nextVersion
        properties.nextVersion == '1.0.0'
    }

    def "should read nextVersion from deprecated 'release.nextVersion' property"() {
        given:
        project.extensions.extraProperties.set('release.nextVersion', '1.0.0')

        when:
        NextVersionProperties properties = NextVersionPropertiesFactory.create(project, config)

        then:
        properties.nextVersion
        properties.nextVersion == '1.0.0'
    }
}
