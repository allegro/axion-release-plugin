package pl.allegro.tech.build.axion.release

import org.assertj.core.api.Assertions.assertThat
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
        git = Git.init().setInitialBranch("main").setDirectory(projectDir.toFile()).call()
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
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("Some changes applied").setSign(false).setAllowEmpty(true).call()
        git.tag().setName("v1.2.4").call()
        git.commit().setMessage("Some changes applied").setSign(false).setAllowEmpty(true).call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        assertEquals(
            "Project version: 1.2.5",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `release version property overrides calculated version`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion", "-Prelease.version=9.9.9")
            .withPluginClasspath()
            .build()

        assertEquals(
            "Project version: 9.9.9",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `currentVersion prints version without prefix when release quiet is set`() {
        git.tag().setName("v1.2.3").call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("currentVersion", "-Prelease.quiet=true")
            .withPluginClasspath()
            .build()

        val lines = result.output.lines().filter { it.isNotBlank() && !it.startsWith(">") && !it.startsWith("BUILD") }
        assertThat(lines).anyMatch { it.trim() == "1.2.3" }
    }

    @Test
    fun `exposes previousVersion on scmVersion extension`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        buildFile.writeText(
            $$"""
            plugins { id("pl.allegro.tech.build.axion.release") }
            tasks.register("printPreviousVersion") {
                doLast { println("Previous: ${scmVersion.previousVersion}") }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printPreviousVersion")
            .withPluginClasspath()
            .build()

        assertEquals(
            "Previous: 1.2.3",
            result.output.lines().first { it.contains("Previous:") }
        )
    }

    @Test
    fun `exposes position with branch and revision on scmVersion extension`() {
        buildFile.writeText(
            $$"""
            plugins { id("pl.allegro.tech.build.axion.release") }
            tasks.register("printPosition") {
                doLast {
                    println("Branch: ${scmVersion.position.branch}")
                    println("HasRevision: ${scmVersion.position.revision.length == 40}")
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printPosition")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("Branch: main")
        assertThat(result.output).contains("HasRevision: true")
    }

    @Test
    fun `dry run skips tag creation`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("createRelease", "-Prelease.dryRun=true")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("DRY RUN")
        val tags = git.tagList().call().map { it.name }
        assertThat(tags).doesNotContain("refs/tags/v1.2.4")
    }

    @Test
    fun `plugin registers preRelease and postRelease lifecycle tasks`() {
        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("tasks", "--group=Release")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("preRelease")
        assertThat(result.output).contains("postRelease")
        assertThat(result.output).contains("postPushRelease")
    }

    @Test
    fun `preRelease hook runs before createRelease`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        buildFile.writeText(
            $$"""
            plugins { id("pl.allegro.tech.build.axion.release") }
            tasks.named("preRelease") {
                doLast { println("PRE_RELEASE_HOOK_RAN") }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("createRelease", "-Prelease.dryRun=true")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("PRE_RELEASE_HOOK_RAN")
    }

    @Test
    fun `incrementMinorIfNotOnRelease bumps minor on non-release branches`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        buildFile.writeText(
            $$"""
            plugins { id("pl.allegro.tech.build.axion.release") }
            scmVersion {
                releaseBranches = listOf("main", "master")
                versionIncrementer = incrementMinorIfNotOnRelease()
            }
            tasks.register("printVersion") {
                doLast { println("Project version: ${project.version}") }
            }
            """.trimIndent()
        )

        // Switch to a feature branch so incrementMinorIfNotOnRelease bumps MINOR
        git.checkout().setCreateBranch(true).setName("feature/new-thing").call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        assertEquals(
            "Project version: 1.3.0-feature-new-thing-SNAPSHOT",
            result.output.lines().first { it.contains("Project version:") }
        )
    }

    @Test
    fun `custom snapshotCreator lambda receives version and position`() {
        git.tag().setName("v1.2.3").call()
        git.commit().setMessage("change").setSign(false).setAllowEmpty(true).call()

        buildFile.writeText(
            $$"""
            import pl.allegro.tech.build.axion.release.version.SnapshotCreator
            plugins { id("pl.allegro.tech.build.axion.release") }
            scmVersion {
                snapshotCreator = SnapshotCreator { _, position -> "-${position.shortRevision}-SNAPSHOT" }
            }
            tasks.register("printVersion") {
                doLast { println("Project version: ${project.version}") }
            }
            """.trimIndent()
        )

        git.checkout().setCreateBranch(true).setName("feature/x").call()

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments("printVersion")
            .withPluginClasspath()
            .build()

        // Should contain short revision hash (7 chars) in the snapshot version
        val version = result.output.lines().first { it.contains("Project version:") }
        assertThat(version).matches("Project version: 1\\.2\\.4-[0-9a-f]{7}-SNAPSHOT")
    }

}
