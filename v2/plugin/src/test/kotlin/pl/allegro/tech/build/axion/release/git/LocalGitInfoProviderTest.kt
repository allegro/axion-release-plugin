package pl.allegro.tech.build.axion.release.git

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.util.SystemReader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import pl.allegro.tech.build.axion.release.JgitEmptyConfigSystemReader
import java.nio.file.Path

class LocalGitInfoProviderTest {

    @TempDir
    private lateinit var repoDir: Path
    private lateinit var git: Git
    private val provider = LocalGitInfoProvider()

    @BeforeEach
    fun setup() {
        SystemReader.setInstance(JgitEmptyConfigSystemReader(repoDir))
        git = Git.init().setInitialBranch("main").setDirectory(repoDir.toFile()).call()
        commit("Initial commit")
    }

    @Test
    fun `returns branch name`() {
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.branch).isEqualTo("main")
    }

    @Test
    fun `returns null currentTag when HEAD is not tagged`() {
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.currentTag).isNull()
    }

    @Test
    fun `returns currentTag when HEAD is tagged`() {
        tag("v1.2.3")
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.currentTag).isEqualTo("v1.2.3")
        assertThat(info.commitsSinceTag).isEqualTo(0)
    }

    @Test
    fun `currentTag respects tag prefix — ignores tags with different prefix`() {
        tag("module2-v2.0.0")
        val info = provider.getInfo(repoDir.toFile(), "module1-v")
        assertThat(info!!.currentTag).isNull()
    }

    @Test
    fun `returns latestTag and commitsSinceTag when ahead of last tag`() {
        tag("v1.0.0")
        commit("after tag")
        commit("after tag 2")

        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.latestTag).isEqualTo("v1.0.0")
        assertThat(info.commitsSinceTag).isEqualTo(2)
        assertThat(info.currentTag).isNull()
    }

    @Test
    fun `returns null latestTag when no tags exist`() {
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.latestTag).isNull()
        assertThat(info.commitsSinceTag).isEqualTo(0)
    }

    @Test
    fun `picks up the correct tag in a monorepo setup`() {
        tag("module1-v1.0.0")
        tag("module2-v2.0.0")
        commit("change")

        val info = provider.getInfo(repoDir.toFile(), "module1-v")
        assertThat(info!!.latestTag).isEqualTo("module1-v1.0.0")
    }

    @Test
    fun `uses branchOverride when provided (detached HEAD on CI scenario)`() {
        val info = provider.getInfo(repoDir.toFile(), "v", branchOverride = "main")
        assertThat(info!!.branch).isEqualTo("main")
    }

    @Test
    fun `uses currentTagOverride when provided`() {
        // Simulate CI knowing the tag without us having to detect it via JGit
        tag("v3.0.0")
        val info = provider.getInfo(repoDir.toFile(), "v", currentTagOverride = "v3.0.0")
        assertThat(info!!.currentTag).isEqualTo("v3.0.0")
        assertThat(info.commitsSinceTag).isEqualTo(0)
    }

    @Test
    fun `detects shallow clone`() {
        repoDir.resolve(".git/shallow").toFile().createNewFile()
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.isShallow).isTrue()
    }

    @Test
    fun `returns revision and shortRevision for HEAD commit`() {
        val info = provider.getInfo(repoDir.toFile(), "v")
        assertThat(info!!.revision).hasSize(40)
        assertThat(info.shortRevision).isEqualTo(info.revision.substring(0, 7))
    }

    @Test
    fun `revision is consistent across calls`() {
        val first = provider.getInfo(repoDir.toFile(), "v")!!.revision
        commit("another commit")
        val second = provider.getInfo(repoDir.toFile(), "v")!!.revision
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `returns null when directory is not a git repository`(@TempDir notARepo: Path): Unit {
        assertThat(provider.getInfo(notARepo.toFile(), "v")).isNull()
    }

    @Test
    fun `exponential unshallow strategy finds tag in shallow clone`(@TempDir tmpDir: Path) {
        SystemReader.setInstance(JgitEmptyConfigSystemReader(tmpDir))

        // 1. Build a "remote" repo: 5 commits, then tag v1.0.0, then 3 more commits
        val remoteDir = tmpDir.resolve("remote").toFile()
        val remote = Git.init().setInitialBranch("main").setDirectory(remoteDir).call()
        repeat(5) { i -> remote.commit().setMessage("c$i").setSign(false).setAllowEmpty(true).call() }
        remote.tag().setName("v1.0.0").call()
        repeat(3) { i -> remote.commit().setMessage("after$i").setSign(false).setAllowEmpty(true).call() }
        remote.close()

        // 2. Shallow clone with depth=1 — v1.0.0 is 8 commits back, unreachable
        val shallowDir = tmpDir.resolve("shallow").toFile()
        val shallowClone = Git.cloneRepository()
            .setURI(remoteDir.toURI().toString())
            .setDirectory(shallowDir)
            .setDepth(1)
            .call()
        shallowClone.close()

        assertThat(shallowDir.resolve(".git/shallow")).exists()

        // 3. getInfo with remote name "origin" — strategy deepens and finds v1.0.0
        val info = provider.getInfo(shallowDir, "v", remote = "origin")
        assertThat(info).isNotNull
        assertThat(info!!.latestTag).isEqualTo("v1.0.0")
        assertThat(info.commitsSinceTag).isEqualTo(3)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun commit(message: String) {
        git.commit().setMessage(message).setSign(false).setAllowEmpty(true).call()
    }

    private fun tag(name: String) {
        git.tag().setName(name).call()
    }
}
