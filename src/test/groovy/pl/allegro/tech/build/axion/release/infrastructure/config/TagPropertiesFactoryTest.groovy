package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class TagPropertiesFactoryTest extends Specification {

    private Project project

    private VersionConfig config

    def setup() {
        project = Fixtures.project()
        config = Fixtures.versionConfig(project)
    }

    def "should pick default prefix if none branch prefixes match"() {
        given:
        config.tag.prefix.set( 'default-prefix')
        config.tag.branchPrefix.set( [
                'some.*': 'some-prefix'
        ])

        when:
        TagProperties tagProperties = TagPropertiesFactory.create(config.tag, 'master')

        then:
        tagProperties.prefix == 'default-prefix'
    }

    def "should pick prefix suitable for current branch if defined in per branch prefixes"() {
        given:
        config.tag.prefix = 'default-prefix'
        config.tag.branchPrefix = [
                'some.*': 'some-prefix'
        ]

        when:
        TagProperties tagProperties = TagPropertiesFactory.create(config.tag, 'someBranch')

        then:
        tagProperties.prefix == 'some-prefix'
    }
}
