@file:Suppress("UnstableApiUsage")

package pl.allegro.tech.build

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import pl.allegro.tech.build.execution.ExecutionContexts
import pl.allegro.tech.build.extension.ProjectTaglineExtension
import pl.allegro.tech.build.extension.TaglineExtension
import pl.allegro.tech.build.repository.RepositoryMetadataSource
import pl.allegro.tech.build.repository.RepositoryTagsSource
import pl.allegro.tech.build.versioning.VersionResolution

class TaglinePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create<TaglineExtension>("tagline")

        settings.gradle.settingsEvaluated {
            val collected: Map<String, String> = ExecutionContexts.variables.mapNotNull { name ->
                settings.providers.environmentVariable(name).orNull?.let { name to it }
            }.toMap()

            val metadata = settings.providers.of(RepositoryMetadataSource::class.java) {
                parameters.backend.set(extension.backend)
                parameters.environment.set(collected)
                parameters.directory.set(settings.settingsDir)
            }.get()

            val resolution = VersionResolution(
                metadata = metadata,
                defaultBranchOverride = extension.versioning.defaultBranch.orNull,
                initialVersion = extension.initialVersion.get(),
                initialVersionOverride = settings.providers.gradleProperty("tagline.initialVersion").orNull,
                branchConfig = extension.versioning.branches::forBranch,
            )
            val tags = settings.providers.of(RepositoryTagsSource::class.java) {
                parameters.backend.set(extension.backend)
                parameters.environment.set(collected)
                parameters.directory.set(settings.settingsDir)
                parameters.prefix.set(resolution.config.tagPrefix)
                parameters.tagLookup.set(resolution.config.tagLookup)
                parameters.offline.set(settings.gradle.startParameter.isOffline)
            }.get()

            val version = resolution.resolve(tags)
            extension.versionInfo.value(version).disallowChanges()
            configureProjectVersion(version)
        }
    }

    private fun Settings.configureProjectVersion(version: VersionInfo) {
        settings.gradle.lifecycle.beforeProject {
            project.version = version.currentVersion
            project.extensions.add("tagline", ProjectTaglineExtension(version))

            if (project.path == ":") {
                project.tasks.register<CurrentVersionTask>("currentVersion") {
                    group = "versioning"
                    description = "Prints the resolved build version"
                    currentVersion.set(version.currentVersion)
                }
            }
        }
    }
}
