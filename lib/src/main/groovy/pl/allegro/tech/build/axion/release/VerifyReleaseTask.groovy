package pl.allegro.tech.build.axion.release

import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext

abstract class VerifyReleaseTask extends BaseAxionTask {

    @Input
    abstract SetProperty<String> getSnapshotDependencies()

    @TaskAction
    void verify() {
        VersionResolutionContext context = resolutionContext()

        ScmRepository repository = context.repository()
        ScmChangesPrinter changesPrinter = context.changesPrinter()
        LocalOnlyResolver localOnlyResolver = context.localOnlyResolver()

        def dryRun = versionConfig.dryRun.get()

        checkUncommittedChanges(repository, changesPrinter, dryRun)
        checkAheadOfRemote(repository, localOnlyResolver, dryRun)
        checkSnapshotDependencies()
    }

    private void checkUncommittedChanges(ScmRepository repository, ScmChangesPrinter changesPrinter, boolean dryRun) {
        if (versionConfig.checks.checkUncommittedChanges().get()) {
            boolean uncommittedChanges = repository.checkUncommittedChanges()
            logger.quiet("Looking for uncommitted changes.. ${uncommittedChanges ? 'FAILED' : ''}")
            if (uncommittedChanges) {
                changesPrinter.printChanges()
                if (!dryRun) {
                    throw new IllegalStateException("There are uncommitted files in your repository - can't release. " +
                        "See above for list of all changes.")
                }
            }
        } else {
            logger.quiet('Skipping uncommitted changes check')
        }
    }

    private void checkAheadOfRemote(ScmRepository repository, LocalOnlyResolver localOnlyResolver, boolean dryRun) {
        boolean remoteAttached = repository.remoteAttached(versionConfig.repository.remote.get())
        def localOnly = localOnlyResolver.localOnly(remoteAttached)
        if (versionConfig.checks.checkAheadOfRemote().get() && !localOnly) {
            boolean aheadOfRemote = repository.checkAheadOfRemote()
            logger.quiet("Checking if branch is ahead of remote.. ${aheadOfRemote ? 'FAILED' : ''}")
            if (aheadOfRemote && !dryRun) {
                throw new IllegalStateException("Current branch is ahead of remote counterpart - can't release.")
            }
        } else {
            logger.quiet("Skipping ahead of remote check")
        }
    }

    private void checkSnapshotDependencies() {
        if (versionConfig.checks.checkSnapshotDependencies().get()) {
            def snapshotVersions = snapshotDependencies.get()
            logger.quiet("Checking for snapshot versions.. ${!snapshotVersions.empty ? 'FAILED' : ''}")
            if (!snapshotVersions.empty) {
                throw new IllegalStateException("The project uses snapshot versions - can't release. Snapshots found: " + snapshotVersions)
            }
        } else {
            logger.quiet("Skipping snapshot dependencies check")
        }
    }
}
