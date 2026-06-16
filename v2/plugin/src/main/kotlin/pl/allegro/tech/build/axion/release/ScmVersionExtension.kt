package pl.allegro.tech.build.axion.release

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.git.ScmPosition
import pl.allegro.tech.build.axion.release.version.SnapshotCreator
import pl.allegro.tech.build.axion.release.version.VersionCreator
import pl.allegro.tech.build.axion.release.version.VersionIncrementer
import java.io.File

open class ScmVersionExtension(private val project: Project) {

    val tag = TagConfig()
    val snapshot = SnapshotConfig()
    val hooks = HooksConfig()
    val repository = RepositoryConfig()

    var releaseBranches: List<String> = listOf("main", "master")

    var versionIncrementer: VersionIncrementer = VersionIncrementer.PATCH
    var branchVersionIncrementer: Map<String, VersionIncrementer> = emptyMap()
    var snapshotCreator: SnapshotCreator = SnapshotCreator.SIMPLE
    var versionCreator: VersionCreator = VersionCreator.SIMPLE

    // Set by the plugin after SCM resolution — read-only from user scripts
    var version: String = "unspecified"
        internal set
    var previousVersion: String? = null
        internal set
    var position: ScmPosition = ScmPosition.UNKNOWN
        internal set

    fun tag(configure: TagConfig.() -> Unit) = configure(tag)
    fun snapshot(configure: SnapshotConfig.() -> Unit) = configure(snapshot)
    fun hooks(configure: HooksConfig.() -> Unit) = configure(hooks)
    fun repository(configure: RepositoryConfig.() -> Unit) = configure(repository)

    /**
     * Convenience factory: returns a [VersionIncrementer] that bumps MINOR on non-release branches
     * and PATCH on release branches. Reads the current [releaseBranches] value — call it after
     * setting `releaseBranches`.
     */
    fun incrementMinorIfNotOnRelease(): VersionIncrementer =
        VersionIncrementer.incrementMinorIfNotOnRelease(releaseBranches)

    internal fun effectiveSnapshotCreator(): SnapshotCreator =
        if (snapshotCreator === SnapshotCreator.SIMPLE) SnapshotCreator.simple(snapshot.suffix, snapshot.separator)
        else snapshotCreator
}

class TagConfig {
    // null = auto-detect: "v" for root project, "${project.name}-v" for subprojects
    var prefix: String? = null
    var initialVersion: String = "0.0.1"

    internal fun resolvedPrefix(project: Project): String =
        prefix ?: if (project.parent == null) "v" else "${project.name}-v"
}

class SnapshotConfig {
    var suffix: String = "SNAPSHOT"
    var separator: String = "-"
}

class RepositoryConfig {
    var remote: String = "origin"

    // SSH key — provide content or path (content takes precedence over path)
    var customKey: String? = null
    var customKeyFile: File? = null
    var customKeyPassword: String? = null

    // HTTPS credentials
    var customUsername: String? = null
    var customPassword: String? = null

    // When true (default), exponentially deepens a shallow clone until a tag is found.
    // Disable when running outside CI where the remote is not available.
    var unshallowRepoOnCI: Boolean = true

    // When true, picks the highest semver tag (not the nearest ancestor) as the base version.
    // Useful when multiple release branches merge into each other (e.g. release/1.20.x → release/1.21.x).
    var useHighestVersion: Boolean = false
}

data class HookContext(
    val releaseVersion: String,
    val previousVersion: String?,
    val project: Project
)

class HooksConfig {
    internal val preHooks = mutableListOf<(HookContext) -> Unit>()
    internal val postHooks = mutableListOf<(HookContext) -> Unit>()

    fun pre(action: (HookContext) -> Unit) {
        preHooks.add(action)
    }

    fun post(action: (HookContext) -> Unit) {
        postHooks.add(action)
    }
}
