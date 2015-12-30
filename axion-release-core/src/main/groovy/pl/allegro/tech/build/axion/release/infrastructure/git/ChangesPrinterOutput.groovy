package pl.allegro.tech.build.axion.release.infrastructure.git

interface ChangesPrinterOutput {

    enum ChangeType {
        MODIFIED, ADDED, REMOVED
    }

    void printHeader(String text)

    void printChangeSet(ChangeType type, String label, Set<String> files)

    void printEmptyLine()
}
