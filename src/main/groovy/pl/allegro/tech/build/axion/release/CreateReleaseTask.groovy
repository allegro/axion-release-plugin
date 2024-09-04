package pl.allegro.tech.build.axion.release


import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class CreateReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        ScmService scmService = context.scmService()
        ReleaseBranchesConfiguration releaseBranchesConfiguration = new ReleaseBranchesConfiguration(
            scmService.isReleaseOnlyOnReleaseBranches(),
            context.repository().currentPosition().getBranch(),
            scmService.getReleaseBranchNames()
        )
        ConfigurationCacheConfiguration configurationCacheConfiguration = new ConfigurationCacheConfiguration(
            scmService.isUpdateProjectVersionAfterRelease(),
            (version) -> project.setVersion(version)
        )
        releaser.release(context.rules(), releaseBranchesConfiguration, configurationCacheConfiguration)
    }
}
