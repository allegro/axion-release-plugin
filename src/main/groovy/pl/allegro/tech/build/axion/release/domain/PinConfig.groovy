package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

class PinConfig {
    @Input
    boolean enabled = false

    @OutputFile
    abstract RegularFileProperty pinFile

    @Inject
    PinConfig(Project project) {
        pinFile = project.objects.fileProperty().convention(project.layout.projectDirectory.file("pinned-version.json"))
    }
}
