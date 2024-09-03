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
        return providers.gradleProperty(name)
    }

    protected Provider<Set<String>> gradlePropertyAsSet(String name) {
        return gradleProperty(name).map({ it.tokenize(',') as Set })
    }

    protected Provider<Boolean> gradlePropertyAsBoolean(String name) {
        return gradleProperty(name).map(Boolean::valueOf)
    }

    protected Provider<Boolean> gradlePropertyPresent(String name) {
        return gradleProperty(name).map({ true })
    }
}
