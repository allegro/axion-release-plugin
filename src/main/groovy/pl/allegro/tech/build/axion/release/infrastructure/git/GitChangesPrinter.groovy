package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Status
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutput.Style
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter

class GitChangesPrinter implements ScmChangesPrinter {

    private final StyledTextOutput output

    private final GitRepository repository

    GitChangesPrinter(GitRepository repository, StyledTextOutput output) {
        this.repository = repository
        this.output = output
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
        printChangeset(Style.Description, 'modified', changeset.modified)
        printChangeset(Style.Identifier, 'added', changeset.added)
        printChangeset(Style.Failure, 'removed', changeset.removed)
    }

    private void printChangeset(Style style, String type, Set<String> changes) {
        if (!changes.empty) {
            changes.each { outputLine(style, type, it) }
        }
    }

    private void outputHeader(String text) {
        output.withStyle(Style.Header).println('')
        output.withStyle(Style.Header).println("$text")
    }

    private void outputLine(Style style, String description, String file) {
        output.withStyle(style).append('    ').text("$description: ")
        output.println(file)
    }
}
