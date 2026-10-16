package pl.allegro.tech.build.extension

data class SnapshotConfig(
    val enabled: Boolean,
    val dirty: Boolean,
    val suffix: String,
)

data class BranchConfig(
    val tagPrefix: String,
    val increment: Increment,
    val useHighestVersion: Boolean,
    val sanitize: Boolean,
    val snapshot: SnapshotConfig,
    val versionCreator: VersionCreator,
    val release: Boolean,
)
