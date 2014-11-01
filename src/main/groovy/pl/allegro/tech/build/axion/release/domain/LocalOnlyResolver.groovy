package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class LocalOnlyResolver {

    private static final String LOCAL_ONLY = "release.localOnly"

    private final Project project

    private final boolean localOnly

    LocalOnlyResolver(VersionConfig config, Project project) {
        this.localOnly = config.localOnly
        this.project = project
    }

    boolean localOnly(boolean remoteAttached) {
        if (project.hasProperty(LOCAL_ONLY)) {
            return true
        }
        if(localOnly) {
            return true
        }
        return !remoteAttached
    }
}
