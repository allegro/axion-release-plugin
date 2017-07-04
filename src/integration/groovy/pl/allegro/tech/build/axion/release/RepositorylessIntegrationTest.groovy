package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RepositorylessIntegrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File directory

    void setup() {
        directory = temporaryFolder.root
    }

    def "should not fail build when calling currentVersion task on project without repo"() {
        given:
        File build = temporaryFolder.newFile('build.gradle')
        build << """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }

        project.version = scmVersion.version
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments('currentVersion', '-s')
            .build()

        then:
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }
}
