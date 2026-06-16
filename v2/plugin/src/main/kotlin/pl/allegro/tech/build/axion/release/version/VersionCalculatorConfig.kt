package pl.allegro.tech.build.axion.release.version

data class VersionCalculatorConfig(
    val releaseBranches: List<String> = listOf("main", "master"),
    val initialVersion: String = "0.0.1",
    val versionIncrementer: VersionIncrementer = VersionIncrementer.PATCH,
    val branchVersionIncrementer: Map<String, VersionIncrementer> = emptyMap(),
    val snapshotCreator: SnapshotCreator = SnapshotCreator.SIMPLE,
    val versionCreator: VersionCreator = VersionCreator.SIMPLE
)
