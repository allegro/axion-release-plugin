package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.config.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.properties.ChecksProperties

class ChecksPropertiesFactory {

    private static final String DISABLE_UNCOMMITTED_CHANGES_CHECK = "release.disableUncommittedCheck"

    private static final String DISABLE_AHEAD_OF_REMOTE_CHECK = "release.disableRemoteCheck"

    private static final String DISABLE_CHECKS = "release.disableChecks"

    static ChecksProperties create(Project project, ChecksConfig config) {
        return new ChecksProperties(
                checkUncommittedChanges: checkUncommittedChanges(project, config),
                checkAheadOfRemote: checkAheadOfRemote(project, config)
        )
    }

    private static boolean checkUncommittedChanges(Project project, ChecksConfig config) {
        return project.hasProperty(DISABLE_CHECKS) || project.hasProperty(DISABLE_UNCOMMITTED_CHANGES_CHECK) ? false : config.uncommittedChanges
    }

    private static boolean checkAheadOfRemote(Project project, ChecksConfig config) {
        return project.hasProperty(DISABLE_CHECKS) || project.hasProperty(DISABLE_AHEAD_OF_REMOTE_CHECK) ? false : config.aheadOfRemote
    }

}
