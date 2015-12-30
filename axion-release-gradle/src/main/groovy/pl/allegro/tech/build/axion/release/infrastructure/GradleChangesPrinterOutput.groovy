package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.logging.StyledTextOutput
import pl.allegro.tech.build.axion.release.infrastructure.git.ChangesPrinterOutput

class GradleChangesPrinterOutput implements ChangesPrinterOutput {

    private static final Map STYLE_MAPPING = [
            ADDED   : StyledTextOutput.Style.Identifier,
            MODIFIED: StyledTextOutput.Style.Description,
            REMOVED : StyledTextOutput.Style.Failure
    ]

    private final StyledTextOutput output

    GradleChangesPrinterOutput(StyledTextOutput output) {
        this.output = output
    }

    @Override
    void printHeader(String text) {
        output.withStyle(StyledTextOutput.Style.Header).println('')
        output.withStyle(StyledTextOutput.Style.Header).println("$text")
    }

    @Override
    void printChangeSet(ChangesPrinterOutput.ChangeType type, String label, Set<String> files) {
        files.each { printLine(STYLE_MAPPING[type], label, it) }
    }

    private void printLine(StyledTextOutput.Style style, String label, String file) {
        output.withStyle(style).append('    ').text("$label: ")
        output.println(file)
    }

    @Override
    void printEmptyLine() {
        output.println()
    }
}
