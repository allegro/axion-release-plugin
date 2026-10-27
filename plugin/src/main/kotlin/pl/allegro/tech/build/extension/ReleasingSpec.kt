package pl.allegro.tech.build.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

abstract class ReleasingSpec @Inject constructor(objects: ObjectFactory) {
    val annotation: AnnotationSpec = objects.newInstance<AnnotationSpec>()

    fun annotation(action: Action<in AnnotationSpec>) = action.execute(annotation)
}
