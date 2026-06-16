package pl.allegro.tech.build.axion.release.git.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.testing.GitLab
import pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource

class GitlabCiProviderTest {

    private val provider = GitlabCiProvider()

    @Test
    @GitLab
    fun `is applicable when GITLAB_CI is true`(env: EnvironmentSource) {
        assertThat(provider.isApplicable(env)).isTrue()
    }

    @Test
    fun `is not applicable when GITLAB_CI env var is absent`() {
        assertThat(provider.isApplicable(MapEnvironmentSource(emptyMap()))).isFalse()
    }

    @Test
    @GitLab(commitBranch = "main", commitTag = "")
    fun `returns branch name for a branch build`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("main")
        assertThat(provider.getCurrentTag(env, "v")).isNull()
    }

    @Test
    @GitLab(commitBranch = "feature/my-feature", commitTag = "")
    fun `returns full branch name for feature branches`(env: EnvironmentSource) {
        assertThat(provider.getBranch(env)).isEqualTo("feature/my-feature")
    }

    @Test
    @GitLab(commitTag = "v1.2.3", commitBranch = "")
    fun `returns tag for a tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "v")).isEqualTo("v1.2.3")
    }

    @Test
    @GitLab(commitTag = "module1-v1.2.3", commitBranch = "")
    fun `returns tag for a monorepo tag build matching prefix`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isEqualTo("module1-v1.2.3")
    }

    @Test
    @GitLab(commitTag = "module2-v2.0.0", commitBranch = "")
    fun `returns null when tag does not match prefix (different module)`(env: EnvironmentSource) {
        assertThat(provider.getCurrentTag(env, "module1-v")).isNull()
    }
}
