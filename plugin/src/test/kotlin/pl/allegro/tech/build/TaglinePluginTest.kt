package pl.allegro.tech.build

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test

class TaglinePluginTest {
    private val exampleDir = File("../examples/single-module").canonicalFile

    @Test
    fun `can run task`() {
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(exampleDir)
            .build()
    }
}
