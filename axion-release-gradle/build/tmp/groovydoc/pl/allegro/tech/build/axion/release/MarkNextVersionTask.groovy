package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareContext

class MarkNextVersionTask extends DefaultTask {

    @TaskAction
    void release() {
        Context context = GradleAwareContext.create(project)

        NextVersionMarker marker = new NextVersionMarker(context.scmService())

        Properties rules = context.rules()
        marker.markNextVersion(rules.nextVersion, rules.tag)
    }

}
