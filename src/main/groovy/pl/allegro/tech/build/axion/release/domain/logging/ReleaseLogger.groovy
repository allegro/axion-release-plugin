package pl.allegro.tech.build.axion.release.domain.logging

import pl.allegro.tech.build.axion.release.infrastructure.output.GradleReleaseLoggerFactory

interface ReleaseLogger {

    void trace(String message)

    void debug(String message)

    void info(String message)

    void warn(String message)

    void error(String message)

    void quiet(String message)

    static class Factory {

        private static ReleaseLoggerFactory factory = new GradleReleaseLoggerFactory()

        static void initialize(ReleaseLoggerFactory factory) {
            this.factory = factory
        }

        static ReleaseLogger logger(Class<?> clazz) {
            return factory.logger(clazz)
        }

        static ReleaseLogger logger(String name) {
            return factory.logger(name)
        }

    }
}
