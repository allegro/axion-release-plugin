package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.SnapshotDependenciesChecker
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.ChecksProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext

class VerifyReleaseTask extends DefaultTask {

    @Optional
    VersionConfig versionConfig

    @TaskAction
    void verify() {
        VersionConfig config = GradleAwareContext.configOrCreateFromProject(project, versionConfig)
        Context context = GradleAwareContext.create(project, config)

        ScmRepository repository = context.repository()
        ScmChangesPrinter changesPrinter = context.changesPrinter()

        boolean dryRun = context.rules().dryRun
        ChecksProperties checksRules = context.rules().checks
        LocalOnlyResolver localOnlyResolver = context.localOnlyResolver()

        if (checksRules.checkUncommittedChanges) {
            boolean uncommittedChanges = repository.checkUncommittedChanges()
            project.logger.quiet("Looking for uncommitted changes.. ${uncommittedChanges ? 'FAILED' : ''}")
            if (uncommittedChanges) {
                changesPrinter.printChanges()
                if (!dryRun) {
                    throw new IllegalStateException("There are uncommitted files in your repository - can't release. " +
                            "See above for list of all changes.")
                }
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

        if (checksRules.checkSnapshotDependencies) {
            SnapshotDependenciesChecker checker = new SnapshotDependenciesChecker();
            Collection<String> snapshotVersions = checker.snapshotVersions(project)
            project.logger.quiet("Checking for snapshot versions.. ${!snapshotVersions.empty ? 'FAILED' : ''}")
            if (!snapshotVersions.empty) {
                throw new IllegalStateException("The project uses snapshot versions - can't release. Snapshots found: " + snapshotVersions)
            }
        } else {
            project.logger.quiet("Skipping snapshot dependencies check")
        }
    }
}
