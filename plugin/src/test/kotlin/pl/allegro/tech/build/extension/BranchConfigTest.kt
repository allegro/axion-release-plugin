package pl.allegro.tech.build.extension

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder

class BranchConfigTest {
    private val tagline = ProjectBuilder.builder().build().objects.newInstance<TaglineExtension>()
    private val branches get() = tagline.versioning.branches

    @Test
    fun `non-default branch gets defaults from all`() {
        val config = branches.forBranch("feature/foo")

        assertEquals("v", config.tagPrefix)
        assertEquals(TagLookup.HIGHEST_GLOBAL, config.tagLookup)
        assertEquals(VersionCreatorType.VERSION_WITH_BRANCH, config.versionCreator)
        assertTrue(config.snapshot.enabled)
        assertFalse(config.release)
    }

    @Test
    fun `default branch overrides all`() {
        val config = branches.forBranch("main", isDefault = true)

        assertEquals(VersionCreatorType.VERSION, config.versionCreator)
        assertFalse(config.snapshot.enabled)
        assertEquals("-SNAPSHOT", config.snapshot.suffix)
        assertTrue(config.release)
    }

    @Test
    fun `named pattern overrides all and default`() {
        branches.named("release/*") {
            tagPrefix.set("rv")
            tagLookup.set(TagLookup.HIGHEST_REACHABLE)
        }

        assertEquals("rv", branches.forBranch("release/1.2").tagPrefix)
        assertEquals("rv", branches.forBranch("release/1.2", isDefault = true).tagPrefix)
        assertEquals("v", branches.forBranch("releases").tagPrefix)
        assertEquals(TagLookup.HIGHEST_REACHABLE, branches.forBranch("release/1.2", isDefault = true).tagLookup)
        assertEquals(TagLookup.HIGHEST_GLOBAL, branches.forBranch("releases").tagLookup)
    }

    @Test
    fun `missing branch uses all without matching named wildcard rules`() {
        branches.named("*") { tagPrefix.set("branch-v") }

        assertEquals("v", branches.forBranch(null).tagPrefix)
        assertEquals("branch-v", branches.forBranch("feature/foo").tagPrefix)
    }

    @Test
    fun `later matching patterns win`() {
        branches.named("release/*") { tagPrefix.set("a") }
        branches.named("release/hot*") { tagPrefix.set("b") }

        assertEquals("b", branches.forBranch("release/hotfix").tagPrefix)
        assertEquals("a", branches.forBranch("release/1.0").tagPrefix)
    }
}
