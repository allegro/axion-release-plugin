package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.ChecksResolver
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class VerifyReleaseTask extends DefaultTask {

    @TaskAction
    void prepare() {
        Context context = new Context(project)

        ScmRepository repository = context.repository()
        ScmChangesPrinter changesPrinter = context.changesPrinter(services)
        VersionConfig config = context.config()

        boolean dryRun = config.dryRun
        ChecksResolver resolver = context.checksResolver()
        LocalOnlyResolver localOnlyResolver = context.localOnlyResolver()

        if (resolver.checkUncommittedChanges()) {
            boolean uncommittedChanges = repository.checkUncommittedChanges()
            project.logger.quiet("Looking for uncommitted changes.. ${uncommittedChanges ? 'FAILED' : ''}")
            if (uncommittedChanges && !dryRun) {
                changesPrinter.printChanges()

                throw new IllegalStateException("There are uncommitted files in your repository - can't release. " +
                        "See above for list of all changes.")
            }
        } else {
            project.logger.quiet('Skipping uncommitted changes check')
        }

        boolean remoteAttached = repository.remoteAttached(config.repository.remote)
        if (resolver.checkAheadOfRemote() && !localOnlyResolver.localOnly(remoteAttached)) {
            boolean aheadOfRemote = repository.checkAheadOfRemote()
            project.logger.quiet("Checking if branch is ahead of remote.. ${aheadOfRemote ? 'FAILED' : ''}")
            if (aheadOfRemote && !dryRun) {
                throw new IllegalStateException("Current branch is ahead of remote counterpart - can't release.")
            }
        } else {
            project.logger.quiet("Skipping ahead of remote check")
        }
    }
}
