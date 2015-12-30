package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.config.NextVersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
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

        config.serializer = {'serialize'}
        config.deserializer = {'deserialize'}
        config.suffix = 'something'
        config.separator = '='

        when:
        NextVersionProperties rules = NextVersionPropertiesFactory.create(project, config)

        then:
        rules.serializer() == 'serialize'
        rules.deserializer() == 'deserialize'
        rules.suffix == 'something'
        rules.separator == '='
    }

    def "should read nextVersion from 'release.version' property"() {
        given:
        project.extensions.extraProperties.set('release.version', '1.0.0')

        when:
        NextVersionProperties rules = NextVersionPropertiesFactory.create(project, config)

        then:
        rules.nextVersion
        rules.nextVersion == '1.0.0'
    }

    def "should read nextVersion from deprecated 'release.nextVersion' property"() {
        given:
        project.extensions.extraProperties.set('release.nextVersion', '1.0.0')

        when:
        NextVersionProperties rules = NextVersionPropertiesFactory.create(project, config)

        then:
        rules.nextVersion
        rules.nextVersion == '1.0.0'
    }
}
