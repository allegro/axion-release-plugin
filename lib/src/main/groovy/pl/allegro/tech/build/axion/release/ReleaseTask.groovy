package pl.allegro.tech.build.axion.release

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResultOutcome
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext
import pl.allegro.tech.build.axion.release.infrastructure.github.GithubService

abstract class ReleaseTask extends BaseAxionTask {

    @Input
    abstract Property<String> getProjectName()

    @Internal
    abstract Property<GithubService> getGithubService()

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()

        ScmPushResult result = releaser.releaseAndPush(context.rules())

        if (result.outcome === ScmPushResultOutcome.FAILED) {
            def status = result.failureStatus
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote status: ${status}")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException("Status: ${status}\nMessage: ${message}")
        }

        if (result.outcome === ScmPushResultOutcome.SUCCESS) {
            String version = versionConfig.uncached.decoratedVersion
            githubService.get().setOutput("released-version", projectName.get(), version)
        }
    }
}
