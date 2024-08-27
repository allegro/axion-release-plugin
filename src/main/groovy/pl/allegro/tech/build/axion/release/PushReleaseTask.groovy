package pl.allegro.tech.build.axion.release


import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResultOutcome
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class PushReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        ScmPushResult result = releaser.pushRelease()

        if (result.outcome === ScmPushResultOutcome.FAILED) {
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException(message)
        }
    }

}
