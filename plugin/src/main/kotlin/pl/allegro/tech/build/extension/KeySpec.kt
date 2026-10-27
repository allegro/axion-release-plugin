package pl.allegro.tech.build.extension

import org.gradle.api.provider.Property

abstract class KeySpec {
    abstract val path: Property<String>
    abstract val password: Property<String>
}
