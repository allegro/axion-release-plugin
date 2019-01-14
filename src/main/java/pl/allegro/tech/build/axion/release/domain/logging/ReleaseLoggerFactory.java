package pl.allegro.tech.build.axion.release.domain.logging;

public interface ReleaseLoggerFactory {
    ReleaseLogger logger(Class<?> clazz);

    ReleaseLogger logger(String name);
}
