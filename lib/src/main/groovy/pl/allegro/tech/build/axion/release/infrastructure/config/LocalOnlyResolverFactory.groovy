package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig

class LocalOnlyResolverFactory {
    static LocalOnlyResolver create(VersionConfig config) {
        return new LocalOnlyResolver(config.localOnly().get())
    }
}
