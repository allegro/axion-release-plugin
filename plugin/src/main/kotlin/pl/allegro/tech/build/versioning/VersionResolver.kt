package pl.allegro.tech.build.versioning

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.VersionInfo
import pl.allegro.tech.build.extension.Increment
import pl.allegro.tech.build.extension.Position
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

internal object VersionResolver {
    fun resolve(
        metadata: RepositoryMetadata,
        tags: TagSelection,
        config: BranchConfig,
        initialVersion: String,
    ): VersionInfo {
        val initial = Version.tryParse(initialVersion).orElseThrow {
            IllegalArgumentException("tagline.initialVersion must be a SemVer version: $initialVersion")
        }
        val exact = parse(tags.exactTag, config.tagPrefix)
        val base = exact ?: parse(tags.baseTag, config.tagPrefix)
        val candidate = if (base == null) initial else increment(base.version, config.increment)
        if (exact != null) {
            return VersionInfo(
                currentVersion = exact.version.toString(),
                nextVersion = candidate,
                baseVersion = exact.version,
                releaseTag = exact.name,
                revision = metadata.revision,
                branch = metadata.branch,
                snapshot = false,
            )
        }
        val position = Position(metadata.revision, metadata.branch, onTag = false)
        val decorated = config.versionCreator.create(candidate.toString(), position)
        require(decorated.isNotBlank()) { "tagline versionCreator must return a nonempty version" }
        val version = decorated + if (config.snapshot.enabled) config.snapshot.suffix else ""
        val current = if (config.sanitize) version.replace(Regex("[^A-Za-z0-9._-]"), "-") else version
        return VersionInfo(
            currentVersion = current,
            nextVersion = candidate,
            baseVersion = base?.version ?: initial,
            releaseTag = null,
            revision = metadata.revision,
            branch = metadata.branch,
            snapshot = config.snapshot.enabled,
        )
    }

    private fun parse(name: String?, prefix: String): VersionTag? {
        if (name == null) return null
        return requireNotNull(versionTag(name, prefix)) { "Tag $name does not match prefix '$prefix' and SemVer" }
    }

    private fun increment(version: Version, increment: Increment): Version = when (increment) {
        Increment.PATCH -> version.nextPatchVersion()
        Increment.MINOR -> version.nextMinorVersion()
        Increment.MAJOR -> version.nextMajorVersion()
    }
}
