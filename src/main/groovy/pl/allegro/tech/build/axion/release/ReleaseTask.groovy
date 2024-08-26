package pl.allegro.tech.build.axion.release

import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

abstract class ReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        ScmService scmService = context.scmService()
        def releaseBranchNames = scmService.getReleaseBranchNames()
        def currentBranch = context.repository().currentPosition().getBranch()
        def isReleaseOnlyOnDefaultBranches = scmService.isReleaseOnlyOnDefaultBranches()
        ScmPushResult result = releaser.releaseAndPush(context.rules(), isReleaseOnlyOnDefaultBranches, currentBranch, releaseBranchNames)

        if (!result.success) {
            def status = result.failureStatus
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote status: ${status}")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException("Status: ${status}\nMessage: ${message}")
        }

        if (System.getenv().containsKey('GITHUB_ACTIONS')) {
            Files.write(
                Paths.get(System.getenv('GITHUB_OUTPUT')),
                "released-version=${versionConfig.uncached.decoratedVersion}\n".getBytes(),
                StandardOpenOption.APPEND
            )
        }
    }
}
