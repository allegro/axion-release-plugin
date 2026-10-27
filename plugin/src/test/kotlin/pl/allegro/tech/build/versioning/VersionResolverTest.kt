package pl.allegro.tech.build.versioning

import com.github.zafarkhaja.semver.Version
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import pl.allegro.tech.build.extension.Increment
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.extension.VersionCreator
import pl.allegro.tech.build.extension.VersionCreatorType
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

class VersionResolverTest {
    private val metadata = RepositoryMetadata("0123456789", "feature/foo", "main")
    private val config = BranchConfig(
        tagPrefix = "v",
        increment = Increment.PATCH,
        tagLookup = TagLookup.HIGHEST_GLOBAL,
        sanitize = true,
        snapshot = SnapshotConfig(enabled = true, suffix = "-SNAPSHOT"),
        versionCreator = VersionCreatorType.VERSION_WITH_BRANCH,
        release = false,
    )

    @Test
    fun `version contract covers exact tags initial versions increments and prereleases`() {
        data class Case(
            val exact: String?, val base: String?, val increment: Increment,
            val current: String, val next: String, val snapshot: Boolean,
        )
        val cases = listOf(
            Case("v1.2.3", "v9.0.0", Increment.PATCH, "1.2.3", "1.2.4", snapshot = false),
            Case("v2.0.0-rc.1+build.7", null, Increment.MINOR, "2.0.0-rc.1+build.7", "2.1.0", snapshot = false),
            Case(null, null, Increment.MAJOR, "0.1.0-feature-foo-SNAPSHOT", "0.1.0", snapshot = true),
            Case(null, "v1.2.3", Increment.PATCH, "1.2.4-feature-foo-SNAPSHOT", "1.2.4", snapshot = true),
            Case(null, "v1.2.3", Increment.MINOR, "1.3.0-feature-foo-SNAPSHOT", "1.3.0", snapshot = true),
            Case(null, "v1.2.3", Increment.MAJOR, "2.0.0-feature-foo-SNAPSHOT", "2.0.0", snapshot = true),
            Case(null, "v2.0.0-rc.1+build.7", Increment.PATCH, "2.0.1-feature-foo-SNAPSHOT", "2.0.1", snapshot = true),
        )
        for (case in cases) {
            val result = VersionResolver.resolve(
                metadata, TagSelection(case.exact, case.base), config.copy(increment = case.increment), "0.1.0",
            )
            assertEquals(case.current, result.currentVersion, case.toString())
            assertEquals(Version.parse(case.next), result.nextVersion, case.toString())
            assertEquals(Version.parse((case.exact ?: case.base)?.removePrefix("v") ?: "0.1.0"), result.baseVersion)
            assertEquals(case.snapshot, result.snapshot, case.toString())
            assertEquals(case.exact, result.releaseTag)
            assertEquals(metadata.revision, result.revision)
        }
        assertEquals("1.10.0", highestVersionTag(listOf("junk", "v1.9.0", "v1.10.0-rc.1", "v1.10.0"), "v")?.version.toString())
        assertEquals("v1.2.3+z", highestVersionTag(listOf("v1.2.3+z", "v1.2.3+a"), "v")?.name)
    }

    @Test
    fun `custom formatting receives position and sanitization follows snapshot decoration`() {
        val custom = config.copy(versionCreator = VersionCreator { version, position ->
            assertEquals(metadata.revision, position.revision)
            "$version/${position.branch}/${position.shortRevision}"
        })
        val tags = TagSelection(null, "v1.2.3")
        assertEquals("1.2.4-feature-foo-0123456-SNAPSHOT", VersionResolver.resolve(metadata, tags, custom, "0.1.0").currentVersion)
        assertEquals("1.2.4/feature/foo/0123456-SNAPSHOT", VersionResolver.resolve(metadata, tags, custom.copy(sanitize = false), "0.1.0").currentVersion)
        val exact = config.copy(versionCreator = VersionCreator { _, _ -> error("Exact tags must bypass formatting") })
        assertEquals("1.2.3", VersionResolver.resolve(metadata, TagSelection("v1.2.3", null), exact, "0.1.0").currentVersion)
        assertFailsWith<IllegalArgumentException> { VersionResolver.resolve(metadata, tags, config, "1.0.0.beta.1") }
    }
}
