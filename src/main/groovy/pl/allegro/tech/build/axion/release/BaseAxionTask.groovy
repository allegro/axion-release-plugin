package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

import javax.inject.Inject

abstract class BaseAxionTask extends DefaultTask {
    @Nested
    VersionConfig versionConfig

    @Inject
    protected abstract ProjectLayout getLayout();

    @Inject
    protected abstract ProviderFactory getProviders();

    protected VersionResolutionContext resolutionContext() {
        return VersionResolutionContext.create(versionConfig, layout.projectDirectory)
    }
}
