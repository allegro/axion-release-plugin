package pl.allegro.tech.build.axion.release.git.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.testing.Jenkins
import pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource

class JenkinsProviderTest {

    private val provider = JenkinsProvider()

    @Test
    @Jenkins
    fun `is applicable when JENKINS_URL is present`(env: EnvironmentSource) {
        assertThat(provider.isApplicable(env)).isTrue()
    }

    @Test
    fun `is not applicable when JENKINS_URL env var is absent`() {
        assertThat(provider.isApplicable(MapEnvironmentSource(emptyMap()))).isFalse()
    }

    @Test
    @Jenkins(gitBranch = "origin/main")
    fun `strips remote prefix from branch name`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("main")
        assertThat(provider.getCurrentTag(env, "v")).isNull()
    }

    @Test
    @Jenkins(gitBranch = "main")
    fun `returns branch name as-is when no remote prefix present`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("main")
    }

    @Test
    @Jenkins(gitBranch = "origin/feature/my-feature")
    fun `strips only first segment when branch name contains slashes`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("feature/my-feature")
    }

    @Test
    @Jenkins(gitTagName = "v1.2.3", gitBranch = "")
    fun `returns tag for a tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "v")).isEqualTo("v1.2.3")
    }

    @Test
    @Jenkins(gitTagName = "module1-v1.2.3", gitBranch = "")
    fun `returns tag for a monorepo tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isEqualTo("module1-v1.2.3")
    }

    @Test
    @Jenkins(gitTagName = "module2-v2.0.0", gitBranch = "")
    fun `returns null when tag does not match prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isNull()
    }
}
