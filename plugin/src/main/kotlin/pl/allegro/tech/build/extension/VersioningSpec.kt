package pl.allegro.tech.build.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance

abstract class VersioningSpec @Inject constructor(objects: ObjectFactory) {
    abstract val defaultBranch: Property<String>
    val branches: BranchesSpec = objects.newInstance<BranchesSpec>()

    fun branches(action: Action<in BranchesSpec>) = action.execute(branches)
}
