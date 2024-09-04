package pl.allegro.tech.build.axion.release


import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResultOutcome
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import static java.util.stream.Collectors.toList

abstract class ReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        ReleaseBranchesConfiguration releaseBranchesConfiguration = new ReleaseBranchesConfiguration(
            context.scmService().isReleaseOnlyOnReleaseBranches(),
            context.repository().currentPosition().getBranch(),
            context.scmService().getReleaseBranchNames()
        )

        ScmPushResult result = releaser.releaseAndPush(context.rules(), releaseBranchesConfiguration)

        if (result.outcome === ScmPushResultOutcome.FAILED) {
            def status = result.failureStatus
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote status: ${status}")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException("Status: ${status}\nMessage: ${message}")
        }

        if (result.outcome === ScmPushResultOutcome.SUCCESS) {
            if (System.getenv().containsKey('GITHUB_ACTIONS')) {
                Files.write(
                    Paths.get(System.getenv('GITHUB_OUTPUT')),
                    "released-version=${versionConfig.uncached.decoratedVersion}\n".getBytes(),
                    StandardOpenOption.APPEND
                )
            }
            def projectConnection = GradleConnector
                .newConnector()
                .forProjectDirectory(layout.projectDirectory.asFile)
                .connect()

            def tasksExceptCurrentOne = project.gradle.taskGraph.allTasks - this

            tasksExceptCurrentOne.forEach { task ->
                if (!task.state.executed) {
                    task.actions.clear()
                }
            }

            def taskNames = tasksExceptCurrentOne
                .stream()
                .map(Task::getName)
                .collect(toList())

            def buildLauncher = projectConnection
                .newBuild()
                .forTasks(*taskNames)
                .addArguments('-Prelease.localOnly', '-Prelease.disableChecks')
                .setStandardInput(System.in)
                .setStandardOutput(System.out)
                .setStandardError(System.err)
            buildLauncher.run()
        }
    }
}
