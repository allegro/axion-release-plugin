package pl.allegro.tech.build.axion.release

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter

import javax.inject.Inject

@DisableCachingByDefault(because = "Prints current version from SCM - output depends on live repository state")
abstract class OutputCurrentVersionTask extends BaseAxionTask {

    @Input
    @Optional
    abstract Property<Boolean> getQuiet()

    @Inject
    OutputCurrentVersionTask() {
        this.outputs.upToDateWhen { false }
        getQuiet().convention(providers.gradleProperty("release.quiet").map({ true })
            .orElse(false))
    }

    @TaskAction
    void output() {
        boolean quiet = getQuiet().get()

        String outputContent = versionConfig.version
        if (!quiet) {
            outputContent = '\nProject version: ' + outputContent
        }

        OutputWriter output = new OutputWriter()
        output.println(outputContent)
    }
}
