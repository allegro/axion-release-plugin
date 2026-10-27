package pl.allegro.tech.build.versioning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import pl.allegro.tech.build.extension.Increment
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.extension.VersionCreatorType
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

class VersionResolutionTest {
    private val config = BranchConfig(
        tagPrefix = "v",
        increment = Increment.PATCH,
        tagLookup = TagLookup.HIGHEST_GLOBAL,
        sanitize = true,
        snapshot = SnapshotConfig(enabled = true, suffix = "-SNAPSHOT"),
        versionCreator = VersionCreatorType.VERSION,
        release = false,
    )

    @Test
    fun `branch policy uses explicit override then metadata then conventional defaults`() {
        data class Case(val branch: String?, val detected: String?, val override: String?, val isDefault: Boolean)
        val cases = listOf(
            Case("main", "main", null, isDefault = true),
            Case("main", "main", "trunk", isDefault = false),
            Case("trunk", "main", "trunk", isDefault = true),
            Case("main", null, null, isDefault = true),
            Case("master", null, null, isDefault = true),
            Case("feature/foo", null, null, isDefault = false),
            Case("main", "trunk", null, isDefault = false),
            Case(null, null, null, isDefault = false),
        )
        for (case in cases) {
            val selected = mutableListOf<Pair<String?, Boolean>>()
            val resolution = VersionResolution(
                RepositoryMetadata("revision", case.branch, case.detected), case.override, "0.1.0", null,
            ) { branch, isDefault ->
                selected.add(branch to isDefault)
                config
            }
            assertEquals(listOf(case.branch to case.isDefault), selected, case.toString())
            assertEquals(config, resolution.config)
        }
    }

    @Test
    fun `initial version override takes precedence and remains validated on tagged builds`() {
        fun resolution(override: String?): VersionResolution = VersionResolution(
            RepositoryMetadata("revision", "main", "main"), null, "0.2.0", override,
        ) { _, _ -> config }

        assertEquals("0.2.0-SNAPSHOT", resolution(null).resolve(TagSelection(null, null)).currentVersion)
        assertEquals("0.3.0-SNAPSHOT", resolution("0.3.0").resolve(TagSelection(null, null)).currentVersion)
        assertFailsWith<IllegalArgumentException> {
            resolution("invalid").resolve(TagSelection("v1.2.3", "v1.2.3"))
        }
    }

    @Test
    fun `selected branch configuration rejects empty enabled snapshot suffixes`() {
        val invalid = config.copy(snapshot = config.snapshot.copy(suffix = " "))
        assertFailsWith<IllegalArgumentException> {
            VersionResolution(RepositoryMetadata("revision", "main", "main"), null, "0.1.0", null) { _, _ ->
                invalid
            }
        }
        val disabled = config.copy(snapshot = config.snapshot.copy(enabled = false, suffix = ""))
        val resolution = VersionResolution(RepositoryMetadata("revision", "main", "main"), null, "0.1.0", null) { _, _ -> disabled }
        assertEquals("0.1.0", resolution.resolve(TagSelection(null, null)).currentVersion)
    }
}
