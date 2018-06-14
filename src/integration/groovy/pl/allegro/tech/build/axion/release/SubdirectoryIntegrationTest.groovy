package pl.allegro.tech.build.axion.release

import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SubdirectoryIntegrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File directory

    void setup() {
        directory = temporaryFolder.newFolder("subdirectory")
    }

    def "should correctly print current version when project root is in a subdirectory of git worktree"() {
        given:
        File build = new File(directory, 'build.gradle')
        build << """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }
        project.version = scmVersion.version
        """

        and:
        String version = "1.2.3"
        Git repo = Git.init().setDirectory(temporaryFolder.root).call()
        repo.commit().setAll(true).setMessage("Initial commit").call()
        repo.tag().setAnnotated(true).setName("release-${version}").setMessage(version).call()

        when:
        def result = GradleRunner.create()
            .withProjectDir(directory)
            .withPluginClasspath()
            .withArguments('currentVersion', '-q', '-Prelease.quiet')
            .build()

        then:
        result.output.trim() == version
    }
}
