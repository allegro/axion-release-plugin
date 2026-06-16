package pl.allegro.tech.build.axion.release.git.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.testing.GitHub
import pl.allegro.tech.build.axion.release.testing.GitLab
import pl.allegro.tech.build.axion.release.testing.Jenkins
import pl.allegro.tech.build.axion.release.testing.MapEnvironmentSource

class CiDetectorTest {

    @Test
    @GitHub
    fun `detects GitHub Actions`(env: EnvironmentSource) {
        assertThat(CiDetector.detect(env)).isInstanceOf(GithubActionsProvider::class.java)
    }

    @Test
    @GitLab
    fun `detects GitLab CI`(env: EnvironmentSource) {
        assertThat(CiDetector.detect(env)).isInstanceOf(GitlabCiProvider::class.java)
    }

    @Test
    @Jenkins
    fun `detects Jenkins`(env: EnvironmentSource) {
        assertThat(CiDetector.detect(env)).isInstanceOf(JenkinsProvider::class.java)
    }

    @Test
    fun `returns null when no CI environment is detected`() {
        assertThat(CiDetector.detect(MapEnvironmentSource(emptyMap()))).isNull()
    }

    @Test
    @GitHub
    fun `GitHub takes precedence over Jenkins when both vars are present`(env: MapEnvironmentSource) {
        // GitHub Actions runners also set JENKINS_URL in some setups — GitHub wins
        val combined = MapEnvironmentSource(env.toMap() + mapOf("JENKINS_URL" to "http://jenkins.example.com/"))
        assertThat(CiDetector.detect(combined)).isInstanceOf(GithubActionsProvider::class.java)
    }
}
