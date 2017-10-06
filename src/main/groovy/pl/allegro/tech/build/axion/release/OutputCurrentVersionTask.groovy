package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter
import sun.misc.Version

import javax.inject.Inject

class OutputCurrentVersionTask extends DefaultTask {

    @Optional
    VersionConfig versionConfig

    OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void output() {
        boolean quiet = project.hasProperty('release.quiet')
        VersionConfig config = GradleAwareContext.configOrCreateFromProject(project, versionConfig)

        String outputContent = config.version
        if (!quiet) {
            outputContent = '\nProject version: ' + outputContent
        }

        OutputWriter output = new OutputWriter()
        output.println(outputContent)
    }
}
