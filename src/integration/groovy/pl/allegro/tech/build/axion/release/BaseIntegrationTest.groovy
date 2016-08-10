package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class BaseIntegrationTest extends RepositoryBasedTest {

    File buildFile() {
        return temporaryFolder.newFile('build.gradle')
    }

    void buildFile(String contents) {
        buildFile() << """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }

        """ + contents
    }

    GradleRunner gradle() {
        return GradleRunner.create()
                .withProjectDir(directory)
                .withPluginClasspath()
    }

    BuildResult runGradle(String... arguments) {
        return gradle().withArguments(arguments).build()
    }
}
