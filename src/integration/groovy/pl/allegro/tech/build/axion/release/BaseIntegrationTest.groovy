package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class BaseIntegrationTest extends RepositoryBasedTest {

    File buildFile() {
        return new FileTreeBuilder(temporaryFolder).file("build.gradle","")
    }

    File newFile(String name) {
        return new FileTreeBuilder(temporaryFolder).file(name,"")
    }

    void buildFile(String contents) {
        new FileTreeBuilder(temporaryFolder).file("build.gradle", """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }

        """ + contents +
            """

        project.version = scmVersion.version
        """)
    }

    void vanillaBuildFile(String contents) {
        new FileTreeBuilder(temporaryFolder).file("build.gradle", contents)
    }

    GradleRunner gradle() {
        return GradleRunner.create()
            .withProjectDir(temporaryFolder)
            .withPluginClasspath()
    }

    BuildResult runGradle(String... arguments) {
        return gradle().withArguments(arguments).build()
    }

    BuildResult runGradleAndFail(String... arguments) {
        return gradle().withArguments(arguments).buildAndFail()
    }
}
