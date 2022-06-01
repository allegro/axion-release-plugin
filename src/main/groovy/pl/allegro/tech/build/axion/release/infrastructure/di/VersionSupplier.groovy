package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.file.Directory
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.properties.Properties

class VersionSupplier {
    VersionService.DecoratedVersion resolve(VersionConfig config, Directory projectDirectory) {
        def context = VersionResolutionContext.create(config, projectDirectory)
        Properties rules = context.rules()
        def versionService = context.versionService()
        return versionService.currentDecoratedVersion(rules.version, rules.tag, rules.nextVersion)
    }
}

class MemoizedVersionSupplier {
    private VersionService.DecoratedVersion version
    private final VersionSupplier versionSupplier = new VersionSupplier()

    VersionService.DecoratedVersion resolve(VersionConfig config, Directory projectDirectory) {
        if(version != null) {
            return version
        }
        version = versionSupplier.resolve(config, projectDirectory)
        return version
    }
}
