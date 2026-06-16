package pl.allegro.tech.build.axion.release.git

data class ScmPosition(
    val branch: String,
    val revision: String,
    val shortRevision: String,
    val isClean: Boolean
) {
    companion object {
        val UNKNOWN = ScmPosition(branch = "unknown", revision = "", shortRevision = "", isClean = true)
    }
}
