package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Files

class BaseIntegrationTest extends RepositoryBasedTest {

    File buildFile() {
        return new FileTreeBuilder(temporaryFolder).file("build.gradle", "")
    }

    File newFile(String name) {
        return new FileTreeBuilder(temporaryFolder).file(name, "")
    }

    void buildFile(String contents) {
        new FileTreeBuilder(temporaryFolder).file("build.gradle",
            """
            plugins {
                id 'pl.allegro.tech.build.axion-release'
            }

            $contents

            project.version = scmVersion.version
            scmVersion.ignoreGlobalGitConfig = true
            """
        )
    }

    void vanillaBuildFile(String contents) {
        new FileTreeBuilder(temporaryFolder).file("build.gradle", contents)
    }

    void vanillaSubprojectBuildFile(String subprojectName, String contents) {
        new FileTreeBuilder(temporaryFolder).dir(subprojectName) {
            file("build.gradle", contents)
        }
    }

    void vanillaSettingsFile(String contents) {
        new FileTreeBuilder(temporaryFolder).file("settings.gradle", contents)
    }

    void customProjectFile(String path, String contents) {
        new FileTreeBuilder(temporaryFolder).file(path, contents)
    }

    GradleRunner gradle() {
        return GradleRunner.create()
            .withProjectDir(temporaryFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    BuildResult runGradle(String... arguments) {
        def args = []
        args.add("--stacktrace")
        args.add("--configuration-cache")
        args.add("--warning-mode=fail")
        args.addAll(arguments)

        def gradle = gradle()
        try {
            def gradleRequestedVersion = System.getenv("GRADLE_VERSION_TO_TEST")
            if (gradleRequestedVersion !== null && !gradleRequestedVersion.isEmpty()) {
                println("Running test with Gradle version: ${gradleRequestedVersion}")
                gradle.withGradleVersion(gradleRequestedVersion)
            } else {
                println("Running test with default Gradle version")
            }
            return gradle.withArguments(args).build()
        }

        finally {
            def ccDir = new File(temporaryFolder, "build/reports/configuration-cache")
            if (ccDir.exists() && ccDir.isDirectory()) {
                def dirName = ccDir.listFiles().first().name
                def ccReport = new File(ccDir, dirName).listFiles().first()
                def name = "${ccReport.parentFile.name}-${ccReport.name}"
                def targetFile = new File(System.getProperty("java.io.tmpdir"), name)
                targetFile.delete()
                Files.copy(ccReport.toPath(), targetFile.toPath())
                println("Configuration cache report available @ file://${targetFile.absolutePath}")
            }
        }
    }

    BuildResult runGradleAndFail(String... arguments) {
        return gradle().withArguments(arguments).buildAndFail()
    }
}
