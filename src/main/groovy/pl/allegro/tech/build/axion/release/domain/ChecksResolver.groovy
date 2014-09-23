package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class ChecksResolver {

    private static final String DISABLE_UNCOMMITED_CHANGES_CHECK = "release.disableUncommitedCheck"

    private static final String DISABLE_AHEAD_OF_REMOTE_CHECK = "release.disableRemoteCheck"

    private static final String DISABLE_CHECKS = "release.disableChecks"

    private final ChecksConfig config

    private final Project project

    ChecksResolver(ChecksConfig config, Project project) {
        this.config = config
        this.project = project
    }

    boolean checkUncommitedChanges() {
        if(project.hasProperty(DISABLE_CHECKS)) {
            return false
        }
        if(project.hasProperty(DISABLE_UNCOMMITED_CHANGES_CHECK)) {
            return false
        }
        return config.uncommitedChanges
    }

    boolean checkAheadOfRemote() {
        if(project.hasProperty(DISABLE_CHECKS)) {
            return false
        }
        if(project.hasProperty(DISABLE_AHEAD_OF_REMOTE_CHECK)) {
            return false
        }
        return config.aheadOfRemote
    }
}
