package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class VerifyReleaseIntegrationTest extends BaseIntegrationTest {

    def "should print changes in Git as seen by axion-release"() {
        given:
        buildFile('')
        temporaryFolder.newFile('my-uncommitted-file') <<  "hello"

        when:
        def result = runGradle('verifyRelease', '-Prelease.dryRun', '-Prelease.localOnly')

        then:
        result.output.contains("Unstaged changes:")
        result.output.contains('my-uncommitted-file')
        result.task(":verifyRelease").outcome == TaskOutcome.SUCCESS
    }

}
