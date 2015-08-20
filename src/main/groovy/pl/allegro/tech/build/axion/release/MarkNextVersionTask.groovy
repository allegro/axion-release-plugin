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

        Context context = new Context(project)

        NextVersionMarker marker = new NextVersionMarker(
                context.scmService(),
                logger
        )
        marker.markNextVersion(context.config(), (String) project.property(NEXT_VERSION_PROPERTY))
    }

}
