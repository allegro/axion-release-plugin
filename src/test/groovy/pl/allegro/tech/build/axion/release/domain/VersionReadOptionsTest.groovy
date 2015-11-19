package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class VersionReadOptionsTest extends Specification {

    private Project project
    
    private VersionConfig versionConfig
    
    def setup() {
        project = builder().build()
        versionConfig = new VersionConfig(project)
    }
    
    def "should return forceVersion false when project has no 'release.version' property"() {
        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        !options.forceVersion
    }

    def "should return forceVersion false when project has 'release.version' property with empty value"() {
        given:
        project.extensions.extraProperties.set('release.version', '')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        !options.forceVersion
    }

    def "should return forceVersion true when project has 'release.version' property with non-empty value"() {
        given:
        project.extensions.extraProperties.set('release.version', 'version')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        options.forceVersion
        options.forcedVersion == 'version'
    }

    def "should return trimmed forcedVersion when project has 'release.Version' property with leading or trailing spaces"() {
        given:
        project.extensions.extraProperties.set('release.version', ' version ')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        options.forceVersion
        options.forcedVersion == 'version'
    }

    def "should return forceVersion true when project has deprecated 'release.forceVersion' property with non-empty value"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', 'version')

        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        options.forceVersion
        options.forcedVersion == 'version'
    }
    
    def "should return ignore uncommitted changes flag from version config when no project flag present"() {
        given:
        versionConfig.ignoreUncommittedChanges = false
        
        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)
        
        then:
        !options.ignoreUncommittedChanges
    }

    def "should return ignore uncommitted changes as true when project flag present"() {
        given:
        versionConfig.ignoreUncommittedChanges = false
        project.extensions.extraProperties.set('release.ignoreUncommittedChanges', true)
        
        when:
        VersionReadOptions options = VersionReadOptions.fromProject(project, versionConfig)

        then:
        options.ignoreUncommittedChanges
    }

}
