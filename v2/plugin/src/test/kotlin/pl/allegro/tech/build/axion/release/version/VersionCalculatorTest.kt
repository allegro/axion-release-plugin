package pl.allegro.tech.build.axion.release.version

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.allegro.tech.build.axion.release.git.ScmInfo

class VersionCalculatorTest {

    private val defaults = VersionCalculatorConfig()

    // ── No tags in history ───────────────────────────────────────────────────

    @Test
    fun `returns initial version on release branch when no tags exist`() {
        val info = noTags(branch = "main")
        assertThat(calculate(info)).isEqualTo("0.0.1")
    }

    @Test
    fun `returns snapshot version on feature branch when no tags exist`() {
        val info = noTags(branch = "feature/my-feature")
        assertThat(calculate(info)).isEqualTo("0.0.1-feature-my-feature-SNAPSHOT")
    }

    @Test
    fun `slugifies branch name in snapshot version`() {
        val info = noTags(branch = "feature/MY Feature_123!")
        assertThat(calculate(info)).isEqualTo("0.0.1-feature-my-feature-123-SNAPSHOT")
    }

    // ── Exact tag on HEAD ────────────────────────────────────────────────────

    @Test
    fun `returns tag version when HEAD is exactly tagged (release branch)`() {
        val info = taggedHead(tag = "v1.2.3", branch = "main")
        assertThat(calculate(info)).isEqualTo("1.2.3")
    }

    @Test
    fun `returns tag version when HEAD is exactly tagged (feature branch)`() {
        val info = taggedHead(tag = "v1.2.3", branch = "feature/my-feature")
        assertThat(calculate(info)).isEqualTo("1.2.3")
    }

    @Test
    fun `strips monorepo prefix from tag version`() {
        val info = taggedHead(tag = "module1-v2.0.0", branch = "main")
        assertThat(calculate(info, tagPrefix = "module1-v")).isEqualTo("2.0.0")
    }

    // ── Commits ahead of last tag ─────────────────────────────────────────────

    @Test
    fun `increments patch on release branch when commits exist since last tag`() {
        val info = aheadOfTag(tag = "v1.2.3", branch = "main", commits = 3)
        assertThat(calculate(info)).isEqualTo("1.2.4")
    }

    @Test
    fun `increments patch and adds snapshot suffix on feature branch`() {
        val info = aheadOfTag(tag = "v1.2.3", branch = "feature/my-feature", commits = 1)
        assertThat(calculate(info)).isEqualTo("1.2.4-feature-my-feature-SNAPSHOT")
    }

    @Test
    fun `increments patch for monorepo module on release branch`() {
        val info = aheadOfTag(tag = "module1-v1.0.0", branch = "main", commits = 1)
        assertThat(calculate(info, tagPrefix = "module1-v")).isEqualTo("1.0.1")
    }

    @Test
    fun `increments patch for monorepo module on feature branch`() {
        val info = aheadOfTag(tag = "module1-v1.0.0", branch = "feature/new-thing", commits = 2)
        assertThat(calculate(info, tagPrefix = "module1-v")).isEqualTo("1.0.1-feature-new-thing-SNAPSHOT")
    }

    // ── Custom snapshot config ────────────────────────────────────────────────

    @Test
    fun `respects custom snapshot suffix and separator via simple factory`() {
        val config = VersionCalculatorConfig(snapshotCreator = SnapshotCreator.simple("dev", "."))
        val info = noTags(branch = "feature/x")
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("0.0.1-feature-x.dev")
    }

    // ── Version incrementers ──────────────────────────────────────────────────

    @Test
    fun `MINOR incrementer bumps minor and resets patch`() {
        val config = VersionCalculatorConfig(versionIncrementer = VersionIncrementer.MINOR)
        val info = aheadOfTag(tag = "v1.2.3", branch = "main", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.3.0")
    }

    @Test
    fun `MAJOR incrementer bumps major and resets minor and patch`() {
        val config = VersionCalculatorConfig(versionIncrementer = VersionIncrementer.MAJOR)
        val info = aheadOfTag(tag = "v1.2.3", branch = "main", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("2.0.0")
    }

    @Test
    fun `branchVersionIncrementer overrides default incrementer when branch matches regex`() {
        val config = VersionCalculatorConfig(
            releaseBranches = listOf("main", "master", "release/1.x"),
            versionIncrementer = VersionIncrementer.PATCH,
            branchVersionIncrementer = mapOf("release/.*" to VersionIncrementer.MINOR)
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "release/1.x", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.3.0")
    }

    @Test
    fun `branchVersionIncrementer falls back to default when no pattern matches`() {
        val config = VersionCalculatorConfig(
            versionIncrementer = VersionIncrementer.PATCH,
            branchVersionIncrementer = mapOf("release/.*" to VersionIncrementer.MINOR)
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "main", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.2.4")
    }

    @Test
    fun `incrementMinorIfNotOnRelease bumps minor on non-release branch`() {
        val releaseBranches = listOf("main", "release/.*")
        val config = VersionCalculatorConfig(
            releaseBranches = releaseBranches,
            versionIncrementer = VersionIncrementer.incrementMinorIfNotOnRelease(releaseBranches)
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "feature/my-feature", commits = 1)
        // feature branch → increment MINOR
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config))
            .isEqualTo("1.3.0-feature-my-feature-SNAPSHOT")
    }

    @Test
    fun `incrementMinorIfNotOnRelease bumps patch on release branch`() {
        val releaseBranches = listOf("main", "release/1.x")
        val config = VersionCalculatorConfig(
            releaseBranches = releaseBranches,
            versionIncrementer = VersionIncrementer.incrementMinorIfNotOnRelease(releaseBranches)
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "release/1.x", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.2.4")
    }

    // ── Snapshot / version creators ───────────────────────────────────────────

    @Test
    fun `custom lambda snapshotCreator receives version and ScmPosition`() {
        val config = VersionCalculatorConfig(
            snapshotCreator = SnapshotCreator { _, position -> "-${position.shortRevision}-SNAPSHOT" }
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "feature/x", commits = 1)
            .copy(shortRevision = "abc1234")
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config))
            .isEqualTo("1.2.4-abc1234-SNAPSHOT")
    }

    @Test
    fun `WITH_COMMIT_HASH versionCreator appends short revision`() {
        val config = VersionCalculatorConfig(versionCreator = VersionCreator.WITH_COMMIT_HASH)
        val info = aheadOfTag(tag = "v1.2.3", branch = "main", commits = 1).copy(shortRevision = "abc1234")
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.2.4-abc1234")
    }

    @Test
    fun `WITH_BRANCH versionCreator appends branch name to release version`() {
        val config = VersionCalculatorConfig(
            snapshotCreator = SnapshotCreator { _, _ -> null },
            versionCreator = VersionCreator.WITH_BRANCH
        )
        val info = aheadOfTag(tag = "v1.2.3", branch = "feature/my-branch", commits = 1)
        assertThat(VersionCalculator.calculate(info, tagPrefix = "v", config = config)).isEqualTo("1.2.4-feature-my-branch")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun calculate(info: ScmInfo, tagPrefix: String = "v"): String =
        VersionCalculator.calculate(info, tagPrefix = tagPrefix, config = defaults)

    private fun noTags(branch: String) = ScmInfo(
        branch = branch, currentTag = null, latestTag = null,
        commitsSinceTag = 0, isShallow = false, isDirty = false
    )

    private fun taggedHead(tag: String, branch: String) = ScmInfo(
        branch = branch, currentTag = tag, latestTag = tag,
        commitsSinceTag = 0, isShallow = false, isDirty = false
    )

    private fun aheadOfTag(tag: String, branch: String, commits: Int) = ScmInfo(
        branch = branch, currentTag = null, latestTag = tag,
        commitsSinceTag = commits, isShallow = false, isDirty = false
    )
}
