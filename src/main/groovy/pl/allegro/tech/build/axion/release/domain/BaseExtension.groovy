package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion

import javax.inject.Inject

abstract class BaseExtension {
    @Inject
    protected abstract ProviderFactory getProviders()

    @Inject
    protected abstract ProjectLayout getLayout()

    @Inject
    protected abstract ObjectFactory getObjects();

    protected Provider<String> gradleProperty(String name) {
        // Deprecated and a noop starting in 7.4
        if (GradleVersion.current() < GradleVersion.version("7.4")) {
            return providers.gradleProperty(name).forUseAtConfigurationTime()
        } else {
            return provider
        }
    }


    protected Provider<Boolean> gradlePropertyBoolean(String name) {
        return gradleProperty(name).map(Boolean::valueOf)
    }

    protected Provider<Boolean> gradlePropertyPresent(String name) {
        return gradleProperty(name).map({true})
    }
}
