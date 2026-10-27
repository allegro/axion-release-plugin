package pl.allegro.tech.build.extension

import org.gradle.api.provider.Property

abstract class AnnotationSpec {
    abstract val message: Property<AnnotationMessage>

    fun message(creator: AnnotationMessage) = message.set(creator)
}
