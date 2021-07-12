package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class RepositorylessIntegrationTest extends Specification {

    @TempDir
    File temporaryFolder

    def "should not fail build when calling currentVersion task on project without repo"() {
        given:
        new FileTreeBuilder(temporaryFolder).file("build.gradle",
            """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }

        project.version = scmVersion.version
        """)

        when:
        def result = GradleRunner.create()
            .withProjectDir(temporaryFolder)
            .withPluginClasspath()
            .withArguments('currentVersion', '-s')
            .build()

        then:
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }
}
