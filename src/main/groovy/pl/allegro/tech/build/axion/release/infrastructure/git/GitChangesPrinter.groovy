package pl.allegro.tech.build.axion.release.infrastructure.git

import org.eclipse.jgit.api.Status
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
        printChangeset('modified', status.changed)
        printChangeset('added', status.added)
        printChangeset('removed', status.removed)

        outputHeader('Unstaged changes:')
        printChangeset('modified', status.modified)
        printChangeset('added', status.untracked)
        printChangeset('removed', status.missing)

        output.println()
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
