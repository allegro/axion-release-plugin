package pl.allegro.tech.build.axion.release


import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

@DisableCachingByDefault(because = "Creates and pushes next-version SCM tag - side-effectful operation that must always run")
abstract class MarkNextVersionTask extends BaseAxionTask {
    @TaskAction
    void release() {
        VersionResolutionContext context = resolutionContext()
        NextVersionMarker marker = new NextVersionMarker(context.scmService())

        Properties rules = context.rules()
        marker.markNextVersion(rules.nextVersion, rules.tag, versionConfig)
    }
}
