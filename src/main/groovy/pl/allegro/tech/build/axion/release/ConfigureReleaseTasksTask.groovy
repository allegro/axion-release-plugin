package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskDependency
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionContext
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext

/**
 * Task that should set forceIncrementVersion within CreateReleaseTask and ReleaseTask.
 */
class ConfigureReleaseTasksTask extends DefaultTask {

    @Optional
    VersionConfig versionConfig

    @TaskAction
    void configureReleaseTasks() {
        // by default, do not increment the version
        ext.shouldForceIncrementVersion = false

        // determine if there are any changes in this project
        VersionConfig config = GradleAwareContext.configOrCreateFromProject(project, versionConfig)
        Context context = GradleAwareContext.create(project, config)
        VersionService versionService = context.versionService()
        VersionContext versionContext = versionService.currentVersion(
            context.rules().getVersion(), context.rules().getTag(), context.rules().getNextVersion()
        )
        if (versionContext.isSnapshot()) {
            ext.shouldForceIncrementVersion = true
        } else {
            // if there are no changes in this project then we need to check upstream projects for changes
            final Configuration configuration = project.getConfigurations().findByName(ReleasePlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            if (configuration != null) {
                TaskDependency taskDependency = configuration.getTaskDependencyFromProjectDependency(true, ReleasePlugin.CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK)
                for (Task depTask : taskDependency.getDependencies()) {
                    if (depTask.shouldForceIncrementVersion) {
                        // upstream change detected, we should increment the version, no need to check any more
                        ext.shouldForceIncrementVersion = true
                        break
                    }
                }
            }
        }

        // now configure the release tasks to force increment the version if a change was detected upstream
        // if there was a change in this project then the project will be released normally, not forced
        if (!versionContext.isSnapshot()) {
            ReleaseTask releaseTask = (ReleaseTask) getProject().tasks.findByName(ReleasePlugin.RELEASE_TASK)
            releaseTask?.setForceIncrementVersion(shouldForceIncrementVersion)

            CreateReleaseTask createReleaseTask = (CreateReleaseTask) getProject().tasks.findByName(ReleasePlugin.CREATE_RELEASE_TASK)
            createReleaseTask?.setForceIncrementVersion(shouldForceIncrementVersion)
        }
    }

}
