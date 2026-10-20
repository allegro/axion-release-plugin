package pl.allegro.tech.build

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import pl.allegro.tech.build.extension.TaglineExtension

class TaglinePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create<TaglineExtension>("tagline")
        settings.gradle.settingsEvaluated {
            Adapters.getAdapter(extension, EnvironmentVariables { name ->
                settings.providers.environmentVariable(name).orNull
            }, settings.settingsDir)
        }
    }
}
