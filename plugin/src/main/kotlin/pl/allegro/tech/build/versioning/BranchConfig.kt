package pl.allegro.tech.build.versioning

import pl.allegro.tech.build.extension.Increment
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.extension.VersionCreator

internal data class BranchConfig(
    val tagPrefix: String,
    val increment: Increment,
    val tagLookup: TagLookup,
    val sanitize: Boolean,
    val snapshot: SnapshotConfig,
    val versionCreator: VersionCreator,
    val release: Boolean,
)
