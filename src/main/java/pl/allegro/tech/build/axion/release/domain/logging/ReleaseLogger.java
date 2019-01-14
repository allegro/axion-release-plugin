package pl.allegro.tech.build.axion.release.domain.logging;

import pl.allegro.tech.build.axion.release.infrastructure.output.GradleReleaseLoggerFactory;

public interface ReleaseLogger {

    void trace(String message);

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void quiet(String message);

    class Factory {
        private static ReleaseLoggerFactory factory = new GradleReleaseLoggerFactory();

        public static void initialize(ReleaseLoggerFactory factory) {
            Factory.factory = factory;
        }

        public static ReleaseLogger logger(Class<?> clazz) {
            return factory.logger(clazz);
        }

        public static ReleaseLogger logger(String name) {
            return factory.logger(name);
        }
    }
}
