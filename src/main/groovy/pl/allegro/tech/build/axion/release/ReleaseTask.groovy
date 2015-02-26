package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class ReleaseTask extends DefaultTask {

    @TaskAction
    void release() {
        Context context = new Context(project)

        ScmService scmService = context.scmService(project)
        
        Releaser releaser = new Releaser(
                scmService,
                new ReleaseHooksRunner(project.logger, scmService, context.config(project).hooks),
                context.localOnlyResolver(project),
                logger
        )
        releaser.release(context.config(project))
    }
}
