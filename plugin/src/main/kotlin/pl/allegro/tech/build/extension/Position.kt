package pl.allegro.tech.build.extension

data class Position(
    val revision: String,
    val branch: String?,
    val onTag: Boolean,
) {
    val shortRevision: String get() = revision.take(7)
}
