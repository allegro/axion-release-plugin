package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLoggerFactory

class GradleReleaseLoggerFactory implements ReleaseLoggerFactory {

    private final GradleLogger logger

    GradleReleaseLoggerFactory(Logger logger) {
        this.logger = new GradleLogger(logger)
    }

    @Override
    ReleaseLogger logger(Class<?> clazz) {
        return logger
    }

    @Override
    ReleaseLogger logger(String name) {
        return logger
    }

    private static class GradleLogger implements ReleaseLogger {

        private final Logger logger

        GradleLogger(Logger logger) {
            this.logger = logger
        }

        @Override
        void trace(String message) {
            logger.trace(message)
        }

        @Override
        void debug(String message) {
            logger.debug(message)
        }

        @Override
        void info(String message) {
            logger.info(message)
        }

        @Override
        void warn(String message) {
            logger.warn(message)
        }

        @Override
        void error(String message) {
            logger.error(message)
        }

        @Override
        void quiet(String message) {
            logger.quiet(message)
        }
    }
}
