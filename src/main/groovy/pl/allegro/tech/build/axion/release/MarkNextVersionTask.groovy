package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class MarkNextVersionTask extends DefaultTask {

    private static final String NEXT_VERSION_PROPERTY = "release.nextVersion"

    @TaskAction
    void release() {
        if (!project.hasProperty(NEXT_VERSION_PROPERTY)) {
            logger.error("No next version specified! Use -P$NEXT_VERSION_PROPERTY to set next version.")
            return
        }

        Context context = Context.instance(project)

        NextVersionMarker marker = new NextVersionMarker(
                context.scmService(),
                context.localOnlyResolver(),
                logger
        )
        marker.markNextVersion(context.config(), project.property(NEXT_VERSION_PROPERTY))
    }

}
