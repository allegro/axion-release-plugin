package pl.allegro.tech.build.axion.release

import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class CreateReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        def scmService = context.scmService()
        def repository = context.repository()
        def releaseBranchNames = scmService.getReleaseBranchNames()
        def currentBranch = repository.currentPosition().getBranch()
        def isReleaseOnlyOnDefaultBranches = scmService.isReleaseOnlyOnDefaultBranches()
        releaser.release(context.rules(), isReleaseOnlyOnDefaultBranches, currentBranch, releaseBranchNames)
    }
}
