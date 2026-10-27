package pl.allegro.tech.build.extension

fun interface VersionCreator {
    fun create(version: String, position: Position): String
}
