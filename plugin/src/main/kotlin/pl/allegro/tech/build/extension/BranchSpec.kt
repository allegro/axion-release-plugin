package pl.allegro.tech.build.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance

abstract class BranchSpec @Inject constructor(objects: ObjectFactory) {
    abstract val tagPrefix: Property<String>
    abstract val increment: Property<Increment>
    abstract val tagLookup: Property<TagLookup>
    abstract val sanitize: Property<Boolean>
    abstract val versionCreator: Property<VersionCreator>
    abstract val release: Property<Boolean>

    val snapshot: SnapshotSpec = objects.newInstance<SnapshotSpec>()

    fun snapshot(action: Action<in SnapshotSpec>) = action.execute(snapshot)

    fun versionCreator(creator: VersionCreator) = versionCreator.set(creator)

    internal fun applyDefaults() {
        tagPrefix.convention("v")
        increment.convention(Increment.PATCH)
        tagLookup.convention(TagLookup.HIGHEST_GLOBAL)
        sanitize.convention(true)
        versionCreator.convention(VersionCreatorType.VERSION_WITH_BRANCH)
        release.convention(false)
        snapshot.enabled.convention(true)
        snapshot.suffix.convention("-SNAPSHOT")
    }
}
