package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix

class SimpleIntegrationTest extends BaseIntegrationTest {

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
        result.output.contains(fullPrefix() +'blabla')
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
}
