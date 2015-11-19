package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class NextVersionOptions {

    private static final String NEXT_VERSION_PROPERTY = "release.version"

    private static final String DEPRECATED_NEXT_VERSION_PROPERTY = "release.nextVersion"


    final String nextVersion

    NextVersionOptions(String nextVersion) {
        this.nextVersion = nextVersion
    }

    static NextVersionOptions fromProject(Project project, Logger logger) {
        String nextVersion = project.hasProperty(NEXT_VERSION_PROPERTY) ? project.property(NEXT_VERSION_PROPERTY) : null
        if(nextVersion == null && project.hasProperty(DEPRECATED_NEXT_VERSION_PROPERTY)) {
            logger.warn("Using deprecated parameter: $DEPRECATED_NEXT_VERSION_PROPERTY! Use $NEXT_VERSION_PROPERTY instead.")
            nextVersion = project.property(DEPRECATED_NEXT_VERSION_PROPERTY)
        }

        if(nextVersion == null) {
            throw new IllegalArgumentException("No next version specified! Use -P$NEXT_VERSION_PROPERTY to set next version.")
        }

        return new NextVersionOptions(nextVersion)
    }
}
