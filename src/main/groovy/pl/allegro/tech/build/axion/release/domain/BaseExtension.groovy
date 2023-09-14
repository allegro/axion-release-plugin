package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

abstract class BaseExtension {
    @Inject
    protected abstract ProviderFactory getProviders()

    @Inject
    protected abstract ProjectLayout getLayout()

    @Inject
    protected abstract ObjectFactory getObjects();

    protected Provider<String> gradleProperty(String name) {
        // forUseAtConfigurationTime() required to make tests happy
        // TODO: Gradle 7.2+ deprecates forUseAtConfigurationTime(), remove at some future time
        return providers.gradleProperty(name).forUseAtConfigurationTime()
    }

    protected Provider<Boolean> gradlePropertyPresent(String name) {
        return gradleProperty(name).map({true})
    }
}
