package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter

class OutputCurrentVersionTask extends DefaultTask {

    OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void output() {
        VersionConfig versionConfig = project.extensions.getByType(VersionConfig)

        boolean quiet = project.hasProperty('release.quiet')
        String outputContent = versionConfig.version
        if (!quiet) {
            outputContent = '\nProject version: ' + outputContent
        }

        OutputWriter output = new OutputWriter()
        output.println(outputContent)
    }

}
