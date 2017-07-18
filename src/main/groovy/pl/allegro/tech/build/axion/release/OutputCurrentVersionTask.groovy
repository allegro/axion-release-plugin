package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter

class OutputCurrentVersionTask extends DefaultTask {

    @Optional
    VersionConfig versionConfig

    OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void output() {
        boolean quiet = project.hasProperty('release.quiet')
        String outputContent = resolveVersionConfig().version
        if (!quiet) {
            outputContent = '\nProject version: ' + outputContent
        }

        OutputWriter output = new OutputWriter()
        output.println(outputContent)
    }

    def resolveVersionConfig() {
        return this.versionConfig == null ? project.extensions.getByType(VersionConfig) : this.versionConfig
    }

}
