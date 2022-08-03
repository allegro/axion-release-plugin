package pl.allegro.tech.build.axion.release.infrastructure.output

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class OutputWriter {
    private static final Logger logger = Logging.getLogger(OutputWriter.class);

    void println(String text) {
        logger.quiet(text)
    }

}
