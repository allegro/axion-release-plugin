package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import pl.allegro.tech.build.axion.release.domain.VersionConfig

class ReleasePlugin implements Plugin<Project> {

    public static final String VERSION_EXTENSION = 'scmVersion'

    public static final String VERIFY_RELEASE_TASK = 'verifyRelease'

    public static final String RELEASE_TASK = 'release'

    public static final String CREATE_RELEASE_TASK = 'createRelease'

    public static final String PUSH_RELEASE_TASK = 'pushRelease'

    public static final String MARK_NEXT_VERSION_TASK = 'markNextVersion'

    public static final String CURRENT_VERSION_TASK = 'currentVersion'

    public static final String DRY_RUN_FLAG = 'release.dryRun'

    @Override
    void apply(Project project) {
        project.extensions.create(VERSION_EXTENSION, VersionConfig, project)

        project.tasks.register(VERIFY_RELEASE_TASK, VerifyReleaseTask) {
            group = 'Release'
            description = 'Verifies code is ready for release.'
        }

        project.tasks.register(RELEASE_TASK, ReleaseTask) {
            group = 'Release'
            description = 'Performs release - creates tag and pushes it to remote.'
            dependsOn(VERIFY_RELEASE_TASK)
        }

        project.tasks.register(CREATE_RELEASE_TASK, CreateReleaseTask) {
            group = 'Release'
            description = 'Performs first stage of release - creates tag.'
            dependsOn(VERIFY_RELEASE_TASK)
        }

        project.tasks.register(PUSH_RELEASE_TASK, PushReleaseTask) {
            group = 'Release'
            description = 'Performs second stage of release - pushes tag to remote.'
        }

        project.tasks.register(MARK_NEXT_VERSION_TASK, MarkNextVersionTask) {
            group = 'Release'
            description = 'Creates next version marker tag and pushes it to remote.'
        }

        project.tasks.register(CURRENT_VERSION_TASK, OutputCurrentVersionTask) {
            group = 'Help'
            description = 'Prints current project version extracted from SCM.'
        }
    }
}
