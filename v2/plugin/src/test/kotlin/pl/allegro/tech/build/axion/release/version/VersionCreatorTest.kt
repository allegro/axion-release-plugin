package pl.allegro.tech.build.axion.release.version

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.git.ScmInfo

class VersionCreatorTest {

    private val info = ScmInfo("feature/my-branch", null, "v1.2.3", 2, false, false,
        revision = "abc1234def5678", shortRevision = "abc1234")

    @Test
    fun `SIMPLE passes version through unchanged`() {
        assertThat(VersionCreator.SIMPLE.create("1.2.4-feature-my-branch-SNAPSHOT", info))
            .isEqualTo("1.2.4-feature-my-branch-SNAPSHOT")
    }

    @Test
    fun `WITH_BRANCH appends slugified branch name`() {
        assertThat(VersionCreator.WITH_BRANCH.create("1.2.4", info))
            .isEqualTo("1.2.4-feature-my-branch")
    }

    @Test
    fun `WITH_COMMIT_HASH appends short revision`() {
        assertThat(VersionCreator.WITH_COMMIT_HASH.create("1.2.4", info))
            .isEqualTo("1.2.4-abc1234")
    }

    @Test
    fun `WITH_COMMIT_HASH falls back gracefully when no revision available`() {
        val noRevision = info.copy(revision = "", shortRevision = "")
        assertThat(VersionCreator.WITH_COMMIT_HASH.create("1.2.4", noRevision))
            .isEqualTo("1.2.4")
    }
}
