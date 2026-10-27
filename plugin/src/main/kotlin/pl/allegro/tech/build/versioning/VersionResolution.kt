package pl.allegro.tech.build.versioning

import pl.allegro.tech.build.VersionInfo
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

/** Selects and validates version policy independently of the Gradle lifecycle. */
internal class VersionResolution(
    private val metadata: RepositoryMetadata,
    defaultBranchOverride: String?,
    initialVersion: String,
    initialVersionOverride: String?,
    branchConfig: (branch: String?, isDefault: Boolean) -> BranchConfig,
) {
    private val initialVersion = initialVersionOverride ?: initialVersion
    val config: BranchConfig

    init {
        val defaultBranch = defaultBranchOverride ?: metadata.defaultBranch
        val isDefault = metadata.branch != null && (metadata.branch == defaultBranch ||
            (defaultBranch == null && metadata.branch in listOf("main", "master")))
        config = branchConfig(metadata.branch, isDefault)
        require(!config.snapshot.enabled || config.snapshot.suffix.isNotBlank()) {
            "An enabled snapshot requires a nonempty snapshot.suffix"
        }
    }

    fun resolve(tags: TagSelection): VersionInfo = VersionResolver.resolve(metadata, tags, config, initialVersion)
}
