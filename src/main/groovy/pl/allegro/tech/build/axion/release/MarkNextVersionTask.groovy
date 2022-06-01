package pl.allegro.tech.build.axion.release


import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class MarkNextVersionTask extends BaseAxionTask {
    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        NextVersionMarker marker = new NextVersionMarker(context.scmService())

        Properties rules = context.rules()
        marker.markNextVersion(rules.nextVersion, rules.tag, versionConfig)
    }
}
