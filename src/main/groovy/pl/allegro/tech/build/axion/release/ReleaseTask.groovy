package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class ReleaseTask extends DefaultTask {

    @TaskAction
    void release() {
        Context context = Context.instance(project)

        Releaser releaser = new Releaser(
                context.scmService(),
                context.localOnlyResolver(),
                logger
        )
        releaser.release(context.config())
    }
}
