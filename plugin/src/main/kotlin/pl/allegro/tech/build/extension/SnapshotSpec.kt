package pl.allegro.tech.build.extension

import org.gradle.api.provider.Property

abstract class SnapshotSpec {
    abstract val enabled: Property<Boolean>
    abstract val suffix: Property<String>
}
