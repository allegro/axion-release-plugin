package pl.allegro.tech.build

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class TaglinePluginTest {
    private val exampleDir = File("../examples/single-module").canonicalFile

    @Test
    fun `can run task`() {
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("help")
            .withProjectDir(exampleDir)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }
}
