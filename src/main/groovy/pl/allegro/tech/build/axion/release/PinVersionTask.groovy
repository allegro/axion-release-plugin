package pl.allegro.tech.build.axion.release

import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class PinVersionTask extends BaseAxionTask {

    @TaskAction
    void pin() {
        VersionResolutionContext context = resolutionContext()
        VersionService versionService = context.versionService()
        Properties rules = context.rules()
        VersionService.DecoratedVersion version = versionService.pinCurrentVersion(rules.version, rules.tag, rules.nextVersion, rules.pinning)
        logger.lifecycle("Version pinned to: ${version.decoratedVersion}")
    }
}
