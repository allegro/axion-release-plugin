package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class HighestVersionIntegrationTest extends BaseIntegrationTest {

    def "should return tag with highest version when there are multiple releases on single commit"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('release', '-Prelease.version=1.1.0', '-Prelease.localOnly', '-Prelease.disableChecks', '-Prelease.forceSnapshot')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('1.1.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return tag with highest version when there are normal and alpha releases on single commit"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('markNextVersion', '-Prelease.version=2.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('2.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return tag with last version if useHighestVersion is set to false"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('release', '-Prelease.version=1.5.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after release-1.5.0")

        runGradle('release', '-Prelease.version=1.2.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('1.2.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return tag with highest version if useHighestVersion is set to true"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('release', '-Prelease.version=1.5.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after release-1.5.0")

        runGradle('release', '-Prelease.version=1.2.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion', '-Prelease.useHighestVersion')

        then:
        result.output.contains('1.5.1-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return release tag with highest version if useHighestVersion is set to true"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('release', '-Prelease.version=1.5.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after release-1.5.0")

        runGradle('release', '-Prelease.version=1.2.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after release-1.5.0")

        runGradle('release', '-Prelease.localOnly', '-Prelease.disableChecks', '-Prelease.useHighestVersion')

        when:
        def result = runGradle('currentVersion', '-Prelease.useHighestVersion')

        then:
        result.output.contains('1.5.1')
        !result.output.contains('SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }


}
