package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.ComponentFactory

class ReleasePlugin implements Plugin<Project> {

    static final String VERSION_EXTENSION = 'scmVersion'

    static final String VERIFY_RELEASE_TASK = 'verifyRelease'

    static final String RELEASE_TASK = 'release'

    static final String CURRENT_VERSION_TASK = 'currentVersion'

    @Override
    void apply(Project project) {
        VersionConfig config = project.extensions.create(VERSION_EXTENSION, VersionConfig, project)
        config.versionService = ComponentFactory.versionService(project, config)

        Task verifyReleaseTask = project.tasks.create(VERIFY_RELEASE_TASK, VerifyReleaseTask)
        verifyReleaseTask.group = 'Release'
        verifyReleaseTask.description = 'Verifies code is ready for release.'

        Task releaseTask = project.tasks.create(RELEASE_TASK, ReleaseTask)
        releaseTask.group = 'Release'
        releaseTask.description = 'Performs release - creates tag and pushes it to remote.'

        releaseTask.dependsOn(VERIFY_RELEASE_TASK)

        Task currentVersionTask = project.tasks.create(CURRENT_VERSION_TASK, OutputCurrentVersionTask)
        currentVersionTask.group = 'Help'
        currentVersionTask.description = 'Prints current project version extracted from SCM.'
    }
}