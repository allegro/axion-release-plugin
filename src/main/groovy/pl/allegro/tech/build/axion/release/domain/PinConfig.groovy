package pl.allegro.tech.build.axion.release.domain


import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

abstract class PinConfig extends BaseExtension {
    private static final String PINNING_ENABLED_PROPERTY = 'release.pinning'

    @Input
    @Optional
    abstract Property<Boolean> getEnabled()

    Provider<Boolean> enabled() {
        gradlePropertyPresent(PINNING_ENABLED_PROPERTY).orElse(enabled)
    }

    @OutputFile
    abstract RegularFileProperty getPinFile()

    @Inject
    PinConfig(ObjectFactory objects, ProjectLayout layout) {
        getEnabled().convention(false)
        getPinFile().convention(objects.fileProperty().convention(layout.projectDirectory.file("pinned-version.json")))
    }
}
