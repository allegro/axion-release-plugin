package pl.allegro.tech.build.axion.release


import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class CreateReleaseTask extends BaseAxionTask {

    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        Releaser releaser = context.releaser()
        releaser.release(context.rules())
    }
}
