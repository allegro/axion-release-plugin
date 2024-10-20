package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import static java.util.stream.Collectors.toList
import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix

class SimpleIntegrationTest extends BaseIntegrationTest {

    @Rule
    EnvironmentVariables environmentVariablesRule = new EnvironmentVariables()

    def "should return default version on calling currentVersion task on vanilla repo"() {
        given:
        buildFile('')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 0.1.0-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should return only version when calling cV with -Prelease.quiet"() {
        given:
        buildFile('')

        when:
        def result = runGradle('currentVersion', '-Prelease.quiet')

        then:
        result.output.contains('0.1.0-SNAPSHOT')
        !result.output.contains('Project version: ')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should set released-version github output after release task"(String task, List<String> outputs) {
        given:
        def outputFile = File.createTempFile("github-outputs", ".tmp")
        environmentVariablesRule.set("GITHUB_ACTIONS", "true")
        environmentVariablesRule.set("GITHUB_OUTPUT", outputFile.getAbsolutePath())

        vanillaSettingsFile("""
            rootProject.name = 'root-project'

            include 'sub-project'
            """
        )

        vanillaBuildFile("""
            plugins {
                id 'pl.allegro.tech.build.axion-release'
            }

            scmVersion {
                tag {
                    prefix = 'root-project-'
                }
            }
            """
        )

        vanillaSubprojectBuildFile("sub-project", """
            plugins {
                id 'pl.allegro.tech.build.axion-release'
            }

            scmVersion {
                tag {
                    prefix = 'sub-project-'
                }
            }
            """
        )

        repository.tag('root-project-1.0.0')
        repository.tag('sub-project-2.0.0')
        repository.commit(['.'], 'Some commit')

        when:
        runGradle(task, '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        def definedEnvVariables = outputFile.getText().lines().collect(toList())
        definedEnvVariables.size() == outputs.size()
        definedEnvVariables.containsAll(outputs)

        cleanup:
        environmentVariablesRule.clear("GITHUB_ACTIONS", "GITHUB_OUTPUT")

        where:
        task                   || outputs
        'release'              || ['released-version=1.0.1', 'root-project-released-version=1.0.1', 'sub-project-released-version=2.0.1']
        ':release'             || ['released-version=1.0.1', 'root-project-released-version=1.0.1']
        ':sub-project:release' || ['released-version=2.0.1', 'sub-project-released-version=2.0.1']
    }

    def "should set published-version github output after publish task"(String task, List<String> outputs) {
        given:
        def outputFile = File.createTempFile("github-outputs", ".tmp")
        environmentVariablesRule.set("GITHUB_ACTIONS", "true")
        environmentVariablesRule.set("GITHUB_OUTPUT", outputFile.getAbsolutePath())

        vanillaSettingsFile("""
            rootProject.name = 'root-project'

            include 'sub-project'
            """
        )

        vanillaBuildFile("""
            plugins {
                id 'pl.allegro.tech.build.axion-release'
                id 'maven-publish'
            }

            version = '1.0.0'
            """
        )

        vanillaSubprojectBuildFile("sub-project", """
            plugins {
                id 'pl.allegro.tech.build.axion-release'
                id 'maven-publish'
            }

            version = '2.0.0'
            """
        )

        when:
        runGradle(task)

        then:
        def definedEnvVariables = outputFile.getText().lines().collect(toList())
        definedEnvVariables.size() == outputs.size()
        definedEnvVariables.containsAll(outputs)

        cleanup:
        environmentVariablesRule.clear("GITHUB_ACTIONS", "GITHUB_OUTPUT")

        where:
        task                   || outputs
        'publish'              || ['published-version=1.0.0', 'root-project-published-version=1.0.0', 'sub-project-published-version=2.0.0']
        ':publish'             || ['published-version=1.0.0', 'root-project-published-version=1.0.0']
        ':sub-project:publish' || ['published-version=2.0.0', 'sub-project-published-version=2.0.0']
    }

    def "should return released version on calling cV on repo with release commit"() {
        given:
        buildFile('')

        when:
        def releaseResult = runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        releaseResult.task(':release').outcome == TaskOutcome.SUCCESS
        releaseResult.output.contains('Creating tag: ' + fullPrefix() + '1.0.0')

        when:
        def result = runGradle('cV')

        then:
        result.output.contains('1.0.0')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should force returned version with -Prelease.forceVersion flag"() {
        given:
        buildFile('')

        when:
        def result = runGradle('currentVersion', '-Prelease.forceVersion=2.0.0')

        then:
        result.output.contains('Project version: 2.0.0-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should update file in pre release hook"() {
        given:
        File versionFile = newFile('version-file')
        Files.write(versionFile.toPath(), "🚀 Version: 0.1.0".getBytes(StandardCharsets.UTF_8))

        buildFile("""
            scmVersion {
                hooks {
                    pre 'fileUpdate', [files: ['version-file'], pattern: { v, p -> v }, replacement: { v, p -> v }, encoding: 'utf-8']
                    pre 'commit'
                }
            }
        """)

        when:
        runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks', '-s')

        then:
        versionFile.text == "🚀 Version: 1.0.0"
    }

    def "should fail gracefuly when failed to parse tag"() {
        given:
        buildFile('')
        repository.tag(fullPrefix() + 'blabla-1.0.0')

        when:
        def result = gradle().withArguments('cV').buildAndFail()

        then:
        result.output.contains(fullPrefix() + 'blabla')
    }

    def "should use initial version setting"() {
        given:
        buildFile("""
            scmVersion {
                tag {
                    initialVersion({ t, p -> '0.0.1' })
                }
            }
        """)

        when:
        def result = runGradle('cV')

        then:
        result.output.contains('Project version: 0.0.1-SNAPSHOT')
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should skip release when releaseOnlyOnReleaseBranches is true and current branch is not on releaseBranchNames list"() {
        given:
        buildFile("""
            scmVersion {
                releaseOnlyOnReleaseBranches = true
                releaseBranchNames = ['develop', 'release']
            }
        """)

        when:
        def releaseResult = runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        releaseResult.task(':release').outcome == TaskOutcome.SKIPPED
        releaseResult.task(':verifyRelease').outcome == TaskOutcome.SKIPPED
    }

    def "should skip release when releaseOnlyOnReleaseBranches is set by gradle task property and current branch is not on releaseBranchNames list"() {
        given:
        buildFile("")

        when:
        def releaseResult = runGradle('release', '-Prelease.releaseOnlyOnReleaseBranches', '-Prelease.releaseBranchNames=develop,release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        releaseResult.task(':release').outcome == TaskOutcome.SKIPPED
        releaseResult.task(':verifyRelease').outcome == TaskOutcome.SKIPPED
    }

    def "should not skip release when releaseOnlyOnReleaseBranches is true when on master branch (default releaseBranches list)"() {
        given:
        buildFile("""
            scmVersion {
                releaseOnlyOnReleaseBranches = true
            }
        """)

        when:
        def releaseResult = runGradle('release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        releaseResult.task(':release').outcome == TaskOutcome.SUCCESS
        releaseResult.output.contains('Creating tag: ' + fullPrefix() + '1.0.0')
    }

    def "should skip release and no GITHUB_OUTPUT should be written"() {
        given:
        def outputFile = File.createTempFile("github-outputs", ".tmp")
        environmentVariablesRule.set("GITHUB_ACTIONS", "true")
        environmentVariablesRule.set("GITHUB_OUTPUT", outputFile.getAbsolutePath())

        buildFile('')

        when:
        runGradle('release', '-Prelease.releaseOnlyOnReleaseBranches', '-Prelease.releaseBranchNames=develop,release', '-Prelease.version=1.0.0', '-Prelease.localOnly', '-Prelease.disableChecks')

        then:
        outputFile.getText().isEmpty()

        cleanup:
        environmentVariablesRule.clear("GITHUB_ACTIONS", "GITHUB_OUTPUT")
    }
}
