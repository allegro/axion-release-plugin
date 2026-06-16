package pl.allegro.tech.build.axion.release.version

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.git.ScmPosition

class SnapshotCreatorTest {

    @Test
    fun `SIMPLE always returns slugified branch with leading separator`() {
        val position = ScmPosition("feature/my-branch", "", "", isClean = true)
        assertThat(SnapshotCreator.SIMPLE.create("1.0.0", position))
            .isEqualTo("-feature-my-branch-SNAPSHOT")
    }

    @Test
    fun `SIMPLE slugifies underscores and special characters`() {
        val position = ScmPosition("feature/MY_Branch-123!", "", "", isClean = true)
        assertThat(SnapshotCreator.SIMPLE.create("1.0.0", position))
            .isEqualTo("-feature-my-branch-123-SNAPSHOT")
    }

    @Test
    fun `simple factory respects custom suffix and separator`() {
        val position = ScmPosition("feature/x", "", "", isClean = true)
        assertThat(SnapshotCreator.simple("dev", ".").create("1.0.0", position))
            .isEqualTo("-feature-x.dev")
    }

    @Test
    fun `custom lambda receives version and position`() {
        val position = ScmPosition("main", "", "abc1234", isClean = true)
        val creator = SnapshotCreator { _, pos -> "-${pos.shortRevision}-SNAPSHOT" }
        assertThat(creator.create("1.2.3", position)).isEqualTo("-abc1234-SNAPSHOT")
    }
}
