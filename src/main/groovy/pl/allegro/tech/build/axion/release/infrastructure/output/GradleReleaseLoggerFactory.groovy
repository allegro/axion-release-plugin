package pl.allegro.tech.build.axion.release.infrastructure.output

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLoggerFactory

class GradleReleaseLoggerFactory implements ReleaseLoggerFactory {

    @Override
    ReleaseLogger logger(Class<?> clazz) {
        return new GradleReleaseLogger(Logging.getLogger(clazz))
    }

    @Override
    ReleaseLogger logger(String name) {
        return new GradleReleaseLogger(Logging.getLogger(name))
    }

    private static class GradleReleaseLogger implements ReleaseLogger {

        @Delegate
        private final Logger logger

        GradleReleaseLogger(Logger logger) {
            this.logger = logger
        }
    }
}

