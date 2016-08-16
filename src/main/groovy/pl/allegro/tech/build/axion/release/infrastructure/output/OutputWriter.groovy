package pl.allegro.tech.build.axion.release.infrastructure.output

import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger

class OutputWriter {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(OutputWriter)

    void println(String text) {
        logger.quiet(text)
    }

}
