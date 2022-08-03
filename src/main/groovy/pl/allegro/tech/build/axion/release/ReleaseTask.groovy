package pl.allegro.tech.build.axion.release

import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class ReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        ScmPushResult result = releaser.releaseAndPush(context.rules())

        if(!result.success) {
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException(message)
        }
    }
}
