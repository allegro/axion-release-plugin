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
        Provider<String> property = providers.gradleProperty(name)
        // Deprecated and a noop starting in 7.4
        return currentGradleVersion() < GradleVersion.version("7.4") ? property.forUseAtConfigurationTime() : property
    }

    protected GradleVersion currentGradleVersion() {
        GradleVersion.current()
    }

    protected Provider<Set<String>> gradleSetProperty(String name) {
        return gradleProperty(name).map({ it.tokenize(',') as Set })
    }

    protected Provider<Boolean> gradlePropertyBoolean(String name) {
        return gradleProperty(name).map(Boolean::valueOf)
    }

    protected Provider<Boolean> gradlePropertyPresent(String name) {
        return gradleProperty(name).map({ true })
    }
}
