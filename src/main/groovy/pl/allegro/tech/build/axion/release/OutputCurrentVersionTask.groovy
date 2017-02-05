package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter

class OutputCurrentVersionTask extends DefaultTask {

    public OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void output() {
        VersionConfig versionConfig = project.extensions.getByType(VersionConfig)

        OutputWriter output = new OutputWriter()
        output.println('')
        output.println("Project version: ${versionConfig.version}")
    }

}
