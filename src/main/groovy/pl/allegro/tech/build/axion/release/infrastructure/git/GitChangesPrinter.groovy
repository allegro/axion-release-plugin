package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Status
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.infrastructure.output.OutputWriter

class GitChangesPrinter implements ScmChangesPrinter {

    private final OutputWriter output = new OutputWriter()

    private final GitRepository repository

    GitChangesPrinter(GitRepository repository) {
        this.repository = repository
    }

    @Override
    void printChanges() {
        Status status = repository.listChanges()

        outputHeader('Staged changes:')
        printChangeType(status.staged)

        outputHeader('Unstaged changes:')
        printChangeType(status.unstaged)

        output.println()
    }

    private void printChangeType(Status.Changes changeset) {
        printChangeset('modified', changeset.modified)
        printChangeset('added', changeset.added)
        printChangeset('removed', changeset.removed)
    }

    private void printChangeset(String type, Set<String> changes) {
        if (!changes.empty) {
            changes.each { outputLine(type, it) }
        }
    }

    private void outputHeader(String text) {
        output.println('')
        output.println(text)
    }

    private void outputLine(String description, String file) {
        output.println("    $description: $file")
    }
}
