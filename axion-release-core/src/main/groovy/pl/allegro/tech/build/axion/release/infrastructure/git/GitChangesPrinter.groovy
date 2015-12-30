package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Status
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter

class GitChangesPrinter implements ScmChangesPrinter {

    private final ChangesPrinterOutput changesPrinter

    private final GitRepository repository

    GitChangesPrinter(GitRepository repository, ChangesPrinterOutput changesPrinter) {
        this.repository = repository
        this.changesPrinter = changesPrinter
    }

    @Override
    void printChanges() {
        Status status = repository.listChanges()

        changesPrinter.printHeader('Staged changes:')
        printChangeType(status.staged)

        changesPrinter.printHeader('Unstaged changes:')
        printChangeType(status.unstaged)

        changesPrinter.printEmptyLine()
    }

    private void printChangeType(Status.Changes changeset) {
        changesPrinter.printChangeSet(ChangesPrinterOutput.ChangeType.MODIFIED, 'modified', changeset.modified)
        changesPrinter.printChangeSet(ChangesPrinterOutput.ChangeType.ADDED, 'added', changeset.added)
        changesPrinter.printChangeSet(ChangesPrinterOutput.ChangeType.REMOVED, 'removed', changeset.removed)
    }
}
