package pl.allegro.tech.build.axion.release.git.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.testing.GitHub
import pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource

class GithubActionsProviderTest {

    private val provider = GithubActionsProvider()

    @Test
    @GitHub
    fun `is applicable when GITHUB_ACTIONS is true`(env: EnvironmentSource) {
        assertThat(provider.isApplicable(env)).isTrue()
    }

    @Test
    fun `is not applicable when GITHUB_ACTIONS env var is absent`() {
        assertThat(provider.isApplicable(MapEnvironmentSource(emptyMap()))).isFalse()
    }

    @Test
    @GitHub(ref = "refs/heads/main")
    fun `returns branch name for a branch build`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("main")
        assertThat(provider.getCurrentTag(env, "v")).isNull()
    }

    @Test
    @GitHub(ref = "refs/heads/feature/my-feature")
    fun `returns full branch name including slashes for feature branches`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("feature/my-feature")
    }

    @Test
    @GitHub(ref = "refs/tags/v1.2.3")
    fun `returns tag for a tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "v")).isEqualTo("v1.2.3")
    }

    @Test
    @GitHub(ref = "refs/tags/module1-v1.2.3")
    fun `returns tag for a monorepo tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isEqualTo("module1-v1.2.3")
    }

    @Test
    @GitHub(ref = "refs/tags/module2-v2.0.0")
    fun `returns null when tag does not match requested prefix (different module)`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isNull()
    }

    @Test
    @GitHub(ref = "refs/tags/v1.2.3")
    fun `returns tag name as branch for a tag build`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("v1.2.3")
    }

    @Test
    fun `notifyRelease writes released-version to GITHUB_OUTPUT file`(@org.junit.jupiter.api.io.TempDir tmp: java.nio.file.Path) {
        val outputFile = tmp.resolve("github_output").toFile().also { it.createNewFile() }
        val env = pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource(
            mapOf("GITHUB_ACTIONS" to "true", "GITHUB_OUTPUT" to outputFile.absolutePath)
        )
        provider.notifyRelease("1.2.3", env)
        assertThat(outputFile.readText()).isEqualTo("released-version=1.2.3\n")
    }

    @Test
    fun `notifyRelease is a no-op when GITHUB_OUTPUT is not set`() {
        val env = pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource(
            mapOf("GITHUB_ACTIONS" to "true")
        )
        // Should not throw
        provider.notifyRelease("1.2.3", env)
    }
}
