package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver

class LocalOnlyResolverFactory {

    private static final String LOCAL_ONLY = "release.localOnly"

    static LocalOnlyResolver create(Project project, VersionConfig config) {
        boolean baseValue = project.hasProperty(LOCAL_ONLY) || config.localOnly
        return new LocalOnlyResolver(baseValue)
    }

}
