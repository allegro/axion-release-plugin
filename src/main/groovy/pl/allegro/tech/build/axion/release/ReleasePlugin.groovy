package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.SnapshotDependenciesChecker
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.util.FileLoader

abstract class ReleasePlugin implements Plugin<Project> {

    public static final String VERSION_EXTENSION = 'scmVersion'
    public static final String VERIFY_RELEASE_TASK = 'verifyRelease'
    public static final String RELEASE_TASK = 'release'
    public static final String CREATE_RELEASE_TASK = 'createRelease'
    public static final String PUSH_RELEASE_TASK = 'pushRelease'
    public static final String MARK_NEXT_VERSION_TASK = 'markNextVersion'
    public static final String CURRENT_VERSION_TASK = 'currentVersion'

    @Override
    void apply(Project project) {
        FileLoader.setRoot(project.rootDir)

        def versionConfig = project.extensions.create(VERSION_EXTENSION, VersionConfig, project.rootProject.layout.projectDirectory)

        project.tasks.withType(BaseAxionTask).configureEach( {
            it.versionConfig = versionConfig
        })

        project.tasks.register(VERIFY_RELEASE_TASK, VerifyReleaseTask) {
            group = 'Release'
            description = 'Verifies code is ready for release.'
            snapshotDependencies.addAll(
                project.provider({
                    SnapshotDependenciesChecker checker = new SnapshotDependenciesChecker();
                    return checker.snapshotVersions(project)
                })
            )
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
