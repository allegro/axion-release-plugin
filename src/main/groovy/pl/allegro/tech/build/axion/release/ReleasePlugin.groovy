package pl.allegro.tech.build.axion.release

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
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

    private static final String RELEASE_GROUP = "Release"

    public static final String RELEASE_DEPENDENTS_TASK = "releaseDependents"

    public static final String CREATE_RELEASE_DEPENDENTS_TASK = "createReleaseDependents"

    public static final String CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK = "configureReleaseDependentsTasks"

    public static final String TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "testRuntimeClasspath";

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

        registerReleaseDependentTasks(project)

        registerConfigureReleaseTasksTask(project)
    }

    /**
     * Create releaseDependents and createReleaseDependents tasks.
     * The tasks should depend on the release task and be set as a dependency for all project tasks that
     * depend on this project.
     * @param project the project in which to register the tasks.
     */
    void registerReleaseDependentTasks(Project project) {
        project.getTasks().register(CREATE_RELEASE_DEPENDENTS_TASK, new Action<Task>() {
            @Override
            void execute(Task task) {
                task.setDescription("Creates a release for this project and all dependent projects")
                task.setGroup(RELEASE_GROUP)
                task.dependsOn(CREATE_RELEASE_TASK)
                addDependsOnTaskInOtherProjects(task, false,
                    CREATE_RELEASE_DEPENDENTS_TASK, TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                // configure this task to depend on configureReleaseDependentsTasks
                task.dependsOn(CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK)
            }
        })
        project.getTasks().register(RELEASE_DEPENDENTS_TASK, new Action<Task>() {
            @Override
            void execute(Task task) {
                task.setDescription("Creates and pushes a release for this project and all dependent projects")
                task.setGroup(RELEASE_GROUP)
                task.dependsOn(RELEASE_TASK)
                addDependsOnTaskInOtherProjects(task, false,
                    RELEASE_DEPENDENTS_TASK, TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                // configure this task to depend on configureReleaseDependentsTasks
                task.dependsOn(CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK)
            }
        })
    }

    /**
     * Register configureReleaseDependentsTasks task.
     * The task will fire before release.
     * @param project the project in which to register the tasks.
     */
    void registerConfigureReleaseTasksTask(Project project) {
        project.getTasks().register(CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK, ConfigureReleaseTasksTask, new Action<Task>() {
            @Override
            void execute(Task task) {
                task.setDescription("Configures the releaseDependents and createReleaseDependents tasks to increment the release version")
                task.setGroup(RELEASE_GROUP)

                // createReleaseDependents will cause createRelease to fire, so set this task to fire before createRelease
                task.getProject().getTasks().findByName(CREATE_RELEASE_TASK).configure {
                    mustRunAfter(task)
                }
                // releaseDependents will cause release to fire, so set this task to fire before release
                task.getProject().getTasks().findByName(RELEASE_TASK).configure {
                    mustRunAfter(task)
                }
                // configure this task to depend on all similar tasks in upstream projects
                addDependsOnTaskInOtherProjects(task, true,
                    CONFIGURE_RELEASE_DEPENDENTS_TASKS_TASK, TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            }
        })
    }

    /**
     * Adds a dependency on tasks with the specified name in other projects.  The other projects are determined from
     * project lib dependencies using the specified configuration name. These may be projects this project depends on or
     * projects that depend on this project based on the useDependOn argument.
     *
     * @param task Task to add dependencies to
     * @param useDependedOn if true, add tasks from projects this project depends on, otherwise use projects that depend on this one.
     * @param otherProjectTaskName name of task in other projects
     * @param configurationName name of configuration to use to find the other projects. If the configuration does not exist then no dependency will be added.
     */
    private void addDependsOnTaskInOtherProjects(final Task task, boolean useDependedOn, String otherProjectTaskName,
                                                 String configurationName) {
        Project project = task.getProject()
        final Configuration configuration = project.getConfigurations().findByName(configurationName)
        if (configuration != null) {
            task.dependsOn(configuration.getTaskDependencyFromProjectDependency(useDependedOn, otherProjectTaskName))
        }
    }

}
