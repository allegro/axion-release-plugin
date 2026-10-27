package pl.allegro.tech.build.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import pl.allegro.tech.build.versioning.BranchConfig
import pl.allegro.tech.build.versioning.SnapshotConfig

abstract class BranchesSpec @Inject constructor(private val objects: ObjectFactory) {
    val all: BranchSpec = objects.newInstance<BranchSpec>()
    val default: BranchSpec = objects.newInstance<BranchSpec>()
    private val named = linkedMapOf<String, BranchSpec>()

    fun all(action: Action<in BranchSpec>) = action.execute(all)

    fun default(action: Action<in BranchSpec>) = action.execute(default)

    fun named(pattern: String, action: Action<in BranchSpec>) {
        action.execute(named.getOrPut(pattern) { objects.newInstance<BranchSpec>() })
    }

    internal fun forBranch(branch: String?, isDefault: Boolean = false): BranchConfig {
        val layers = buildList {
            add(all)
            if (isDefault) add(default)
            if (branch != null) named.filterKeys { globToRegex(it).matches(branch) }.values.forEach(::add)
        }.asReversed()

        fun <T : Any> resolve(property: (BranchSpec) -> Property<T>): T =
            layers.firstNotNullOfOrNull { property(it).orNull } ?: property(all).get()

        fun <T : Any> resolveSnapshot(property: (SnapshotSpec) -> Property<T>): T =
            resolve { property(it.snapshot) }

        return BranchConfig(
            tagPrefix = resolve { it.tagPrefix },
            increment = resolve { it.increment },
            tagLookup = resolve { it.tagLookup },
            sanitize = resolve { it.sanitize },
            snapshot = SnapshotConfig(
                enabled = resolveSnapshot { it.enabled },
                suffix = resolveSnapshot { it.suffix },
            ),
            versionCreator = resolve { it.versionCreator },
            release = resolve { it.release },
        )
    }

    private fun globToRegex(glob: String): Regex =
        glob.split('*').joinToString(".*") { Regex.escape(it) }.toRegex()
}
