package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class NextVersionOptionsTest extends Specification {

    private Project project

    def setup() {
        project = builder().build()
    }

    def "should read nextVersion from 'release.version' property"() {
        given:
        project.extensions.extraProperties.set('release.version', '1.0.0')

        when:
        NextVersionOptions options = NextVersionOptions.fromProject(project, project.logger)

        then:
        options.nextVersion
        options.nextVersion == '1.0.0'
    }

    def "should read nextVersion from deprecated 'release.nextVersion' property"() {
        given:
        project.extensions.extraProperties.set('release.nextVersion', '1.0.0')

        when:
        NextVersionOptions options = NextVersionOptions.fromProject(project, project.logger)

        then:
        options.nextVersion
        options.nextVersion == '1.0.0'
    }

    def "should throw exception when no 'release.version' property present"() {
        when:
        NextVersionOptions.fromProject(project, project.logger)

        then:
        thrown(IllegalArgumentException)
    }
}
