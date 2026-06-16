package pl.allegro.tech.build.axion.release.testing

import pl.allegro.tech.build.axion.release.git.ci.EnvironmentSource

class MapEnvironmentSource(private val env: Map<String, String>) : EnvironmentSource {
    override fun get(name: String): String? = env[name]
    fun toMap(): Map<String, String> = env
}
