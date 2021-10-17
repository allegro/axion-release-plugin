package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix

class PreviousVersionIntegrationTest extends BaseIntegrationTest {

    def "should return undecorated current version if no previous releases"() {
        given:
        buildFile("""
        task outputDecorated { doLast {
            println "Previous: \${scmVersion.previousVersion}"
        } }
        """)

        when:
        def result = runGradle('outputDecorated')

        then:
        result.output.contains('Previous: 0.1.0')
        result.task(":outputDecorated").outcome == TaskOutcome.SUCCESS
    }

    def "should return previous version even if current commit is tagged"() {
        given:
        buildFile("""
        task outputDecorated { doLast {
            println "Previous: \${scmVersion.previousVersion}"
        } }
        """)

        runGradle('release', '-Prelease.version=1.1.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        repository.commit(['*'], "commit after " + fullPrefix() + "1.1.0")

        runGradle('release', '-Prelease.version=1.2.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle('outputDecorated')

        then:
        result.output.contains('Previous: 1.1.0')
        result.task(":outputDecorated").outcome == TaskOutcome.SUCCESS
    }

}
