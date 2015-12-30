package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.config.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.properties.ChecksProperties
import spock.lang.Specification

class ChecksPropertiesFactoryTest extends Specification {

    private ChecksConfig config = new ChecksConfig()

    private Project project = ProjectBuilder.builder().build()

    def "should return default value from config when no project properties are present"() {
        given:
        config.aheadOfRemote = true
        config.uncommittedChanges = true

        ChecksProperties rules = ChecksPropertiesFactory.create(project, config)

        expect:
        rules.checkUncommittedChanges
        rules.checkAheadOfRemote
    }

    def "should always return false if checks are globally disabled using release.disableChecks"() {
        given:
        project.extensions.extraProperties.set('release.disableChecks', true)
        config.aheadOfRemote = true
        config.uncommittedChanges = true

        ChecksProperties rules = ChecksPropertiesFactory.create(project, config)

        expect:
        !rules.checkUncommittedChanges
        !rules.checkAheadOfRemote
    }

    def "should skip uncommitted changes check if it was disabled using project property"() {
        given:
        project.extensions.extraProperties.set('release.disableUncommittedCheck', true)
        config.uncommittedChanges = true

        ChecksProperties rules = ChecksPropertiesFactory.create(project, config)

        expect:
        !rules.checkUncommittedChanges
    }

    def "should skip ahead of remote check if it was disabled using project property"() {
        given:
        project.extensions.extraProperties.set('release.disableRemoteCheck', true)
        config.aheadOfRemote = true

        ChecksProperties rules = ChecksPropertiesFactory.create(project, config)

        expect:
        !rules.checkAheadOfRemote
    }

}
