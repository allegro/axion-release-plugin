package pl.allegro.tech.build

import org.gradle.api.IsolatedAction
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import pl.allegro.tech.build.execution.ExecutionContexts
import pl.allegro.tech.build.extension.ProjectTaglineExtension
import pl.allegro.tech.build.extension.TaglineExtension
import pl.allegro.tech.build.repository.RepositoryMetadataSource
import pl.allegro.tech.build.repository.RepositoryParameters
import pl.allegro.tech.build.repository.RepositoryTagsSource
import pl.allegro.tech.build.versioning.VersionResolution

class TaglinePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create<TaglineExtension>("tagline")
        settings.gradle.settingsEvaluated {
            val collected = ExecutionContexts.variables.mapNotNull { name ->
                settings.providers.environmentVariable(name).orNull?.let { name to it }
            }.toMap()
            fun configure(parameters: RepositoryParameters) {
                parameters.backend.set(extension.backend)
                parameters.environment.set(collected)
                parameters.directory.set(settings.settingsDir)
            }
            val metadata = settings.providers.of(RepositoryMetadataSource::class.java) {
                configure(parameters)
            }.get()
            val resolution = VersionResolution(
                metadata = metadata,
                defaultBranchOverride = extension.versioning.defaultBranch.orNull,
                initialVersion = extension.initialVersion.get(),
                initialVersionOverride = settings.providers.gradleProperty("tagline.initialVersion").orNull,
                branchConfig = extension.versioning.branches::forBranch,
            )
            val tags = settings.providers.of(RepositoryTagsSource::class.java) {
                configure(parameters)
                parameters.prefix.set(resolution.config.tagPrefix)
                parameters.tagLookup.set(resolution.config.tagLookup)
                parameters.offline.set(settings.gradle.startParameter.isOffline)
            }.get()
            val version = resolution.resolve(tags)
            extension.versionInfo.set(version)
            extension.versionInfo.disallowChanges()
            settings.gradle.lifecycle.beforeProject(ConfigureProjectVersion(version))
        }
    }

    private class ConfigureProjectVersion(private val version: VersionInfo) : IsolatedAction<Project> {
        override fun execute(project: Project) {
            project.extensions.add("tagline", ProjectTaglineExtension(version))
            project.version = version.currentVersion
            if (project.path == ":") {
                project.tasks.register("currentVersion", CurrentVersionTask::class.java) {
                    group = "versioning"
                    description = "Prints the resolved build version"
                    currentVersion.set(version.currentVersion)
                }
            }
        }
    }
}
