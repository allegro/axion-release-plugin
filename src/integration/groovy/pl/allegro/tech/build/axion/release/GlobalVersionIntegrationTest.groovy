package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class GlobalVersionIntegrationTest extends BaseIntegrationTest {
    def "should return tag with highest version if useGlobalVersion is set to true"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')
        runGradle('release', '-Prelease.version=1.5.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after release-1.5.0")

        runGradle('release', '-Prelease.version=1.2.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion', '-Prelease.useGlobalVersion')

        then:
        result.output.contains('1.5.1-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should resolve higher global version when on branch with higher version"() {
        given:
        buildFile('')
        setupABranchWithHighTagAndBBranchWithLowTag(repository)
        repository.checkout('high')

        when:
        def result = runGradle('currentVersion', '-Prelease.useGlobalVersion')

        then:
        result.output.contains('2.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should resolve higher global version when on branch with lower version"() {
        given: 'I am on a branch with a low release tag'
        buildFile('')
        setupABranchWithHighTagAndBBranchWithLowTag(repository)
        repository.checkout('low')

        when:
        def result = runGradle('currentVersion', '-Prelease.useGlobalVersion')

        then:
        result.output.contains('2.0.1')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }
}
