package pl.allegro.tech.build.extension

fun interface AnnotationMessage {
    fun create(position: Position, version: String, previousVersion: String?): String
}
