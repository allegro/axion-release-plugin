package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class SimpleIntegrationTest extends BaseIntegrationTest {

    def "should return default version on calling currentVersion task on vanilla repo"() {
        given:
        buildFile('')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('0.1.0-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return released version on calling cV on repo with release commit"() {
        given:
        buildFile('')

        when:
        def releaseResult = runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        releaseResult.task(':release').outcome == TaskOutcome.SUCCESS
        releaseResult.output.contains('Creating tag: release-1.0.0')

        when:
        def result = runGradle('cV')

        then:
        result.output.contains('1.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

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

    def "should update file in pre release hook"() {
        given:
        File versionFile = newFile('version-file')
        versionFile << "Version: 0.1.0"

        buildFile("""
            scmVersion {
                hooks {
                    pre 'fileUpdate', [files: ['version-file'], pattern: { v, p -> v }, replacement: { v, p -> v }]
                    pre 'commit'
                }
            }
        """)

        when:
        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        versionFile.text == "Version: 1.0.0"
    }
}
