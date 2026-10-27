package pl.allegro.tech.build.execution

internal data class HeaderCredential(val name: String, val value: String) {
    override fun toString(): String = "HeaderCredential(name=$name, value=[redacted])"
}
