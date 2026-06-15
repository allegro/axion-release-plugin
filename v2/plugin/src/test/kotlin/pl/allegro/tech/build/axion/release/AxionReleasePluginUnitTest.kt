package pl.allegro.tech.build.axion.release

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class AxionReleasePluginUnitTest {

    private val project = ProjectBuilder.builder().build().also {
        it.plugins.apply("pl.allegro.tech.build.axion.release")
    }

    @Test
    fun `plugin registers currentVersion task`() {
        assertThat(project.tasks.findByName("currentVersion")).isNotNull()
    }

    @Test
    fun `plugin registers verifyRelease task`() {
        assertThat(project.tasks.findByName("verifyRelease")).isNotNull()
    }

    @Test
    fun `plugin registers createRelease task`() {
        assertThat(project.tasks.findByName("createRelease")).isNotNull()
    }

    @Test
    fun `plugin registers pushRelease task`() {
        assertThat(project.tasks.findByName("pushRelease")).isNotNull()
    }

    @Test
    fun `plugin registers release task`() {
        assertThat(project.tasks.findByName("release")).isNotNull()
    }

    @Test
    fun `plugin creates scmVersion extension`() {
        assertThat(project.extensions.findByName("scmVersion")).isInstanceOf(ScmVersionExtension::class.java)
    }
}
