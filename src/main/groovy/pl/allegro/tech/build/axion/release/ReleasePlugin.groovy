package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import pl.allegro.tech.build.axion.release.domain.VersionConfig

class ReleasePlugin implements Plugin<Project> {

    public static final String VERSION_EXTENSION = 'scmVersion'

    public static final String VERIFY_RELEASE_TASK = 'verifyRelease'

    public static final String RELEASE_TASK = 'release'

    public static final String CONDITIONAL_RELEASE_TASK = 'conditionalRelease'

    public static final String CREATE_RELEASE_TASK = 'createRelease'
    
    public static final String PUSH_RELEASE_TASK = 'pushRelease'

    public static final String MARK_NEXT_VERSION_TASK = 'markNextVersion'

    public static final String CURRENT_VERSION_TASK = 'currentVersion'

    public static final String DRY_RUN_FLAG = 'release.dryRun'

    @Override
    void apply(Project project) {
        project.extensions.create(VERSION_EXTENSION, VersionConfig, project)

        Task verifyReleaseTask = project.tasks.create(VERIFY_RELEASE_TASK, VerifyReleaseTask)
        verifyReleaseTask.group = 'Release'
        verifyReleaseTask.description = 'Verifies code is ready for release.'

        Task releaseTask = project.tasks.create(RELEASE_TASK, ReleaseTask)
        releaseTask.group = 'Release'
        releaseTask.description = 'Performs release - creates tag and pushes it to remote.'
        releaseTask.dependsOn(VERIFY_RELEASE_TASK)

        Task conditionalReleaseTask = project.tasks.create(CONDITIONAL_RELEASE_TASK, ConditionalReleaseTask)
        conditionalReleaseTask.group = 'Release'
        conditionalReleaseTask.description = 'Performs conditional release - if the branch is "releaseBranch", creates a tag and pushes it to remote.'
        conditionalReleaseTask.dependsOn(VERIFY_RELEASE_TASK)

        Task createReleaseTask = project.tasks.create(CREATE_RELEASE_TASK, CreateReleaseTask)
        createReleaseTask.group = 'Release'
        createReleaseTask.description = 'Performs first stage of release - creates tag.'
        createReleaseTask.dependsOn(VERIFY_RELEASE_TASK)

        Task pushReleaseTask = project.tasks.create(PUSH_RELEASE_TASK, PushReleaseTask)
        pushReleaseTask.group = 'Release'
        pushReleaseTask.description = 'Performs second stage of release - pushes tag to remote.'

        Task nextVersionTask = project.tasks.create(MARK_NEXT_VERSION_TASK, MarkNextVersionTask)
        nextVersionTask.group = 'Release'
        nextVersionTask.description = 'Creates next version marker tag and pushes it to remote.'

        Task currentVersionTask = project.tasks.create(CURRENT_VERSION_TASK, OutputCurrentVersionTask)
        currentVersionTask.group = 'Help'
        currentVersionTask.description = 'Prints current project version extracted from SCM.'
    }
}