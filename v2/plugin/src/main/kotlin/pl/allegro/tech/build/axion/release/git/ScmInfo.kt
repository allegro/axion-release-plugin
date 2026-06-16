package pl.allegro.tech.build.axion.release.git

data class ScmInfo(
    val branch: String,
    // tag exactly on HEAD (with prefix, e.g. "v1.2.3"), null if HEAD is not tagged
    val currentTag: String?,
    // nearest ancestor tag (with prefix), null if no tags exist in history
    val latestTag: String?,
    // 0 when currentTag != null, positive when commits exist since latestTag
    val commitsSinceTag: Int,
    val isShallow: Boolean,
    val isDirty: Boolean,
    // full commit SHA of HEAD, empty string when unavailable (e.g. no commits yet)
    val revision: String = "",
    // 7-character abbreviated SHA, empty string when unavailable
    val shortRevision: String = ""
)
