package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.ChecksProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.GradleChangesPrinterOutput
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareContext

class VerifyReleaseTask extends DefaultTask {

    @TaskAction
    void verify() {
        Context context = GradleAwareContext.create(project)

        ScmRepository repository = context.repository()
        ScmChangesPrinter changesPrinter = createChangesPrinter(context)
        VersionConfig config = GradleAwareContext.config(project)

        boolean dryRun = context.rules().dryRun
        ChecksProperties checksRules = context.rules().checks
        LocalOnlyResolver localOnlyResolver = context.localOnlyResolver()

        if (checksRules.checkUncommittedChanges) {
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
        if (checksRules.checkAheadOfRemote && !localOnlyResolver.localOnly(remoteAttached)) {
            boolean aheadOfRemote = repository.checkAheadOfRemote()
            project.logger.quiet("Checking if branch is ahead of remote.. ${aheadOfRemote ? 'FAILED' : ''}")
            if (aheadOfRemote && !dryRun) {
                throw new IllegalStateException("Current branch is ahead of remote counterpart - can't release.")
            }
        } else {
            project.logger.quiet("Skipping ahead of remote check")
        }
    }

    private ScmChangesPrinter createChangesPrinter(Context context) {
        StyledTextOutput output = services.get(StyledTextOutputFactory).create(GradleChangesPrinterOutput)
        return context.changesPrinter(new GradleChangesPrinterOutput(output))
    }
}
