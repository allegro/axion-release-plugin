package pl.allegro.tech.build.axion.release.version

import com.github.zafarkhaja.semver.Version
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.git.ScmInfo

class VersionIncrementerTest {

    private val info = ScmInfo("main", null, "v1.2.3", 1, false, false)

    @Test
    fun `PATCH increments patch component`() {
        assertThat(VersionIncrementer.PATCH.increment(Version.parse("1.2.3"), info).toString())
            .isEqualTo("1.2.4")
    }

    @Test
    fun `MINOR increments minor and resets patch to zero`() {
        assertThat(VersionIncrementer.MINOR.increment(Version.parse("1.2.3"), info).toString())
            .isEqualTo("1.3.0")
    }

    @Test
    fun `MAJOR increments major and resets minor and patch to zero`() {
        assertThat(VersionIncrementer.MAJOR.increment(Version.parse("1.2.3"), info).toString())
            .isEqualTo("2.0.0")
    }
}
