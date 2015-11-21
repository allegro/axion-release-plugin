package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.NextVersionOptions
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class MarkNextVersionTask extends DefaultTask {

    @TaskAction
    void release() {
        NextVersionOptions nextVersionOptions = NextVersionOptions.fromProject(project, logger)
        Context context = new Context(project)

        NextVersionMarker marker = new NextVersionMarker(
                context.scmService(),
                logger
        )

        marker.markNextVersion(context.config(), context.tagNameSerializationRules(), nextVersionOptions.nextVersion)
    }

}
