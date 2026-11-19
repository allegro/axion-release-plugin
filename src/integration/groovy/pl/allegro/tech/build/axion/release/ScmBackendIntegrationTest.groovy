package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class ScmBackendIntegrationTest extends BaseIntegrationTest {

    def "should resolve version with native git backend configured in build file"() {
        given:
        buildFile('''
            scmVersion {
                repository {
                    backend = 'nativeGit'
                }
            }
        ''')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('1.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should resolve version with native git backend configured by gradle property"() {
        given:
        buildFile('')

        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('currentVersion', '-Prelease.scmBackend=nativeGit')

        then:
        result.output.contains('1.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should fail on unknown backend"() {
        given:
        buildFile('')

        when:
        def result = runGradleAndFail('currentVersion', '-Prelease.scmBackend=mercurial')

        then:
        result.output.contains("Unsupported scm backend 'mercurial'")
    }
}
