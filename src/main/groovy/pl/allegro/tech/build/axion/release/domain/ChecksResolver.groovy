package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class ChecksResolver {

    @Deprecated
    private static final String DISABLE_UNCOMMITED_CHANGES_CHECK = "release.disableUncommitedCheck"

    private static final String DISABLE_UNCOMMITTED_CHANGES_CHECK = "release.disableUncommittedCheck"

    private static final String DISABLE_AHEAD_OF_REMOTE_CHECK = "release.disableRemoteCheck"

    private static final String DISABLE_CHECKS = "release.disableChecks"

    private final ChecksConfig config

    private final Project project

    ChecksResolver(ChecksConfig config, Project project) {
        this.config = config
        this.project = project
    }

    boolean checkUncommittedChanges() {
        if(project.hasProperty(DISABLE_CHECKS)) {
            return false
        }
        if(project.hasProperty(DISABLE_UNCOMMITTED_CHANGES_CHECK)) {
            return false
        }
        // Old check for backwards compatibility
        if(project.hasProperty(DISABLE_UNCOMMITED_CHANGES_CHECK)) {
            return false
        }
        return config.uncommittedChanges
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
