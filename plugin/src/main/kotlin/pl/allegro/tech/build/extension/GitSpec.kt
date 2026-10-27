package pl.allegro.tech.build.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance

abstract class GitSpec @Inject constructor(objects: ObjectFactory) {
    abstract val email: Property<String>
    abstract val username: Property<String>
    abstract val password: Property<String>
    abstract val signing: Property<Boolean>
    abstract val requireAheadOfRemote: Property<Boolean>

    val key: KeySpec = objects.newInstance<KeySpec>()

    fun key(action: Action<in KeySpec>) = action.execute(key)
}
