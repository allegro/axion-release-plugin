package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

import java.util.regex.Pattern

class ConditionalReleaseTask  extends DefaultTask {
    String releaseBranch = "master"

    @TaskAction
    void conditionalRelease() {
        println("Release branch is: " + releaseBranch)
        Context context = new Context(project)
        ScmRepository repository = context.repository()
        def branch = repository.currentPosition(Pattern.compile(".*")).branch
        if (branch == releaseBranch) {
            Releaser releaser = context.releaser()

            releaser.release(context.config())
            releaser.pushRelease(context.config())
        }
    }
}
