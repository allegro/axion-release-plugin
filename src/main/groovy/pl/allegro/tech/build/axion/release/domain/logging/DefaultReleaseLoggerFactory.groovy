package pl.allegro.tech.build.axion.release.domain.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultReleaseLoggerFactory implements ReleaseLoggerFactory {

    @Override
    ReleaseLogger logger(Class<?> clazz) {
        return new Slf4jReleaseLogger(LoggerFactory.getLogger(clazz))
    }

    @Override
    ReleaseLogger logger(String name) {
        return new Slf4jReleaseLogger(LoggerFactory.getLogger(name))
    }

    private static class Slf4jReleaseLogger implements ReleaseLogger {

        @Delegate
        private final Logger logger

        Slf4jReleaseLogger(Logger logger) {
            this.logger = logger
        }

        @Override
        void quiet(String message) {
            logger.info(message)
        }
    }
}
