package pl.allegro.tech.build.axion.release

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.util.SystemReader
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals

class AxionReleasePluginTest {

    @TempDir
    private lateinit var projectDir: Path
    private lateinit var buildFile: File
    private lateinit var git: Git

    @BeforeEach
    fun setup() {
        git = Git.init().setDirectory(projectDir.toFile()).call()
        SystemReader.setInstance(JgitEmptyConfigSystemReader(projectDir))
        println("Created a new repository at ${git.repository.directory}")
        buildFile = projectDir.resolve("build.gradle.kts").toFile()
        buildFile.writeText(
            $$"""
            plugins {
                id("pl.allegro.tech.build.axion.release")
            }
            tasks.register("printVersion") {
                doLast {
                    println("Project version: ${project.version}")
                }
            }
        """.trimIndent()
        )
        println("Created `build.gradle.kts` file ${git.repository.directory}")
        git.add().addFilepattern(".").call()
        git.commit().setSign(false).setMessage("Initial commit").call()
    }

    @Test
    fun `should set project to initial RELEASE version when no tags exist (release branch)`() {
        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 0.0.1",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `should set project to initial SNAPSHOT version when no tags exist (feature branch)`() {
        git.checkout().setCreateBranch(true).setName("feature/example-1").call()

        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 0.0.1-feature-example-1-SNAPSHOT",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `should set project to current RELEASE version when tag was found on current HEAD (release branch)`() {
        git.tag().setName("v1.2.3").call()

        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 1.2.3",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `should set project to current RELEASE version when tag was found on current HEAD (feature branch)`() {
        // Create a tag
        git.tag().setName("v1.2.3").call()

        // Switch to a feature branch
        git.checkout().setCreateBranch(true).setName("feature/example-1").call()

        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 1.2.3",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `should set project to next SNAPSHOT version when HEAD is ahead of last release (feature branch)`() {
        // Create a tag
        git.tag().setName("v1.2.3").call()

        // Switch to a feature branch
        git.checkout().setCreateBranch(true).setName("feature/example-1").call()

        // Make some changes and commit them
        git.commit().setMessage("Some changes applied").setSign(false).setAllowEmpty(true).call()

        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 1.2.4-feature-example-1-SNAPSHOT",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `should set project to next RELEASE____________(release branch)`() {
        // Create a tag
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("Some changes applied").setSign(false).setAllowEmpty(true).call()
        git.tag().setName("v1.2.4").call()
        git.commit().setMessage("Some changes applied").setSign(false).setAllowEmpty(true).call()

        // Run the build
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Verify the output
        assertEquals(
            "Project version: 1.2.5",
            result.output.lines().first { it.contains("Project version:") }
        )
    }
}
