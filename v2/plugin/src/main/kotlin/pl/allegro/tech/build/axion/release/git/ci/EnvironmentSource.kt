package pl.allegro.tech.build.axion.release.git.ci

fun interface EnvironmentSource {
    fun get(name: String): String?

    companion object {
        val SYSTEM: EnvironmentSource = EnvironmentSource { System.getenv(it) }
    }
}
