package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class VersionReadOptionsTest extends Specification {

    def "should return forceVersion false when project has no 'release.forceVersion' property"() {
        given:
        Project project = builder().build()

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project)

        then:
        !options.forceVersion
    }

    def "should return forceVersion false when project has 'release.forceVersion' property with empty value"() {
        given:
        Project project = builder().build()
        project.extensions.extraProperties.set('release.forceVersion', '')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project)

        then:
        !options.forceVersion
    }

    def "should return forceVersion true when project has 'release.forceVersion' property with non-empty value"() {
        given:
        Project project = builder().build()
        project.extensions.extraProperties.set('release.forceVersion', 'version')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project)

        then:
        options.forceVersion
        options.forcedVersion == 'version'
    }

}
