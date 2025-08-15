package pl.allegro.tech.build.axion.release

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class AxionReleasePluginUnitTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("pl.allegro.tech.build.axion.release")
        assertNotNull(project.tasks.findByName("release"))
    }
}
