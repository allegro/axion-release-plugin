package pl.allegro.tech.build.extension

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class TaglineExtension @Inject constructor(objects: ObjectFactory) {
    abstract val backend: Property<Backend>

    val versioning: VersioningSpec = objects.newInstance<VersioningSpec>()
    val releasing: ReleasingSpec = objects.newInstance<ReleasingSpec>()
    val git: GitSpec = objects.newInstance<GitSpec>()
    val gitlab: GitlabSpec = objects.newInstance<GitlabSpec>()
    val github: GithubSpec = objects.newInstance<GithubSpec>()

    init {
        backend.convention(Backend.AUTO)

        versioning.branches.all.applyDefaults()
        versioning.branches.default.apply {
            versionCreator.convention(VersionCreatorType.VERSION)
            snapshot.enabled.convention(false)
            release.convention(true)
        }

        releasing.annotation.message.convention(
            AnnotationMessage { _, version, _ -> "Released version $version" }
        )

        git.signing.convention(false)
        git.requireAheadOfRemote.convention(true)
    }

    fun versioning(action: Action<in VersioningSpec>) = action.execute(versioning)

    fun releasing(action: Action<in ReleasingSpec>) = action.execute(releasing)

    fun git(action: Action<in GitSpec>) = action.execute(git)

    fun gitlab(action: Action<in GitlabSpec>) = action.execute(gitlab)

    fun github(action: Action<in GithubSpec>) = action.execute(github)
}

abstract class SnapshotSpec {
    abstract val enabled: Property<Boolean>
    abstract val dirty: Property<Boolean>
    abstract val suffix: Property<String>
}

abstract class BranchSpec @Inject constructor(private val objects: ObjectFactory) {
    abstract val tagPrefix: Property<String>
    abstract val increment: Property<Increment>
    abstract val useHighestVersion: Property<Boolean>
    abstract val sanitize: Property<Boolean>
    abstract val versionCreator: Property<VersionCreator>
    abstract val release: Property<Boolean>

    val snapshot: SnapshotSpec = objects.newInstance<SnapshotSpec>()

    fun snapshot(action: Action<in SnapshotSpec>) = action.execute(snapshot)

    fun versionCreator(creator: VersionCreator) = versionCreator.set(creator)

    internal fun applyDefaults() {
        tagPrefix.convention("v")
        increment.convention(Increment.PATCH)
        useHighestVersion.convention(true)
        sanitize.convention(true)
        versionCreator.convention(VersionCreatorType.VERSION_WITH_BRANCH)
        release.convention(false)
        snapshot.enabled.convention(true)
        snapshot.dirty.convention(true)
        snapshot.suffix.convention("-SNAPSHOT")
    }
}

abstract class BranchesSpec @Inject constructor(private val objects: ObjectFactory) {
    val all: BranchSpec = objects.newInstance<BranchSpec>()
    val default: BranchSpec = objects.newInstance<BranchSpec>()
    private val named = linkedMapOf<String, BranchSpec>()

    val namedBranches: Map<String, BranchSpec> get() = named

    fun all(action: Action<in BranchSpec>) = action.execute(all)

    fun default(action: Action<in BranchSpec>) = action.execute(default)

    fun named(pattern: String, action: Action<in BranchSpec>) {
        action.execute(named.getOrPut(pattern) { objects.newInstance<BranchSpec>() })
    }

    fun forBranch(branch: String, isDefault: Boolean = false): BranchConfig {
        val layers = buildList {
            add(all)
            if (isDefault) add(default)
            named.filterKeys { globToRegex(it).matches(branch) }.values.forEach(::add)
        }.asReversed()

        fun <T : Any> resolve(property: (BranchSpec) -> Property<T>): T =
            layers.firstNotNullOfOrNull { property(it).orNull } ?: property(all).get()

        fun <T : Any> resolveSnapshot(property: (SnapshotSpec) -> Property<T>): T =
            resolve { property(it.snapshot) }

        return BranchConfig(
            tagPrefix = resolve { it.tagPrefix },
            increment = resolve { it.increment },
            useHighestVersion = resolve { it.useHighestVersion },
            sanitize = resolve { it.sanitize },
            snapshot = SnapshotConfig(
                enabled = resolveSnapshot { it.enabled },
                dirty = resolveSnapshot { it.dirty },
                suffix = resolveSnapshot { it.suffix },
            ),
            versionCreator = resolve { it.versionCreator },
            release = resolve { it.release },
        )
    }

    private fun globToRegex(glob: String): Regex =
        glob.split('*').joinToString(".*") { Regex.escape(it) }.toRegex()
}

abstract class VersioningSpec @Inject constructor(objects: ObjectFactory) {
    val branches: BranchesSpec = objects.newInstance<BranchesSpec>()

    fun branches(action: Action<in BranchesSpec>) = action.execute(branches)
}

abstract class AnnotationSpec {
    abstract val message: Property<AnnotationMessage>

    fun message(creator: AnnotationMessage) = message.set(creator)
}

abstract class ReleasingSpec @Inject constructor(objects: ObjectFactory) {
    val annotation: AnnotationSpec = objects.newInstance<AnnotationSpec>()

    fun annotation(action: Action<in AnnotationSpec>) = action.execute(annotation)
}

abstract class KeySpec {
    abstract val path: Property<String>
    abstract val password: Property<String>
}

abstract class GitSpec @Inject constructor(objects: ObjectFactory) {
    abstract val email: Property<String>
    abstract val username: Property<String>
    abstract val password: Property<String>
    abstract val signing: Property<Boolean>
    abstract val requireAheadOfRemote: Property<Boolean>

    val key: KeySpec = objects.newInstance<KeySpec>()

    fun key(action: Action<in KeySpec>) = action.execute(key)
}

abstract class GitlabSpec

abstract class GithubSpec
