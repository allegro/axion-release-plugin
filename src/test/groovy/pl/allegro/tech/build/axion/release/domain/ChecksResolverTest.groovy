package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ChecksResolverTest extends Specification {

    private ChecksConfig config = new ChecksConfig()

    private Project project = ProjectBuilder.builder().build()

    def "should return default value from config when no project properties are present"() {
        given:
        config.aheadOfRemote = true
        config.uncommittedChanges = true

        ChecksResolver resolver = new ChecksResolver(config, project)

        expect:
        resolver.checkUncommittedChanges()
        resolver.checkAheadOfRemote()
    }

    def "should always return false if checks are globally disabled using release.disableChecks"() {
        given:
        project.extensions.extraProperties.set('release.disableChecks', true)
        config.aheadOfRemote = true
        config.uncommittedChanges = true

        ChecksResolver resolver = new ChecksResolver(config, project)

        expect:
        !resolver.checkUncommittedChanges()
        !resolver.checkAheadOfRemote()
    }

    def "should skip uncommitted changes check if it was disabled using project property"() {
        given:
        project.extensions.extraProperties.set('release.disableUncommittedCheck', true)
        config.uncommittedChanges = true

        ChecksResolver resolver = new ChecksResolver(config, project)

        expect:
        !resolver.checkUncommittedChanges()
    }

    // Old check. Tests backwards compatibility with deprecated property
    def "should skip uncommited changes check if it was disabled using project property"() {
        given:
        project.extensions.extraProperties.set('release.disableUncommitedCheck', true)
        config.uncommittedChanges = true

        ChecksResolver resolver = new ChecksResolver(config, project)

        expect:
        !resolver.checkUncommittedChanges()
    }

    def "should skip ahead of remote check if it was disabled using project property"() {
        given:
        project.extensions.extraProperties.set('release.disableRemoteCheck', true)
        config.aheadOfRemote = true

        ChecksResolver resolver = new ChecksResolver(config, project)

        expect:
        !resolver.checkAheadOfRemote()
    }

}
