package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory
import pl.allegro.tech.build.axion.release.domain.VersionConfig

import static org.gradle.logging.StyledTextOutput.Style.*

class OutputCurrentVersionTask extends DefaultTask {

    public OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void output() {
        VersionConfig versionConfig = project.extensions.getByType(VersionConfig)

        StyledTextOutput output = services.get(StyledTextOutputFactory).create(OutputCurrentVersionTask)
        output.withStyle(Header).println('')
        output.withStyle(Header).println("Project version: ${versionConfig.version}")
    }

}
