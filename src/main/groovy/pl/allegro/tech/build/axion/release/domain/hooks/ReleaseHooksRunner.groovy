package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class ReleaseHooksRunner {

    private final Logger logger

    private final ScmService scmService

    private final HooksConfig hooksConfig

    ReleaseHooksRunner(Logger logger, ScmService scmService, HooksConfig hooksConfig) {
        this.logger = logger
        this.scmService = scmService
        this.hooksConfig = hooksConfig
    }

    void runPreReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(logger, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        hooksConfig.preReleaseHooks.each { it.act(context) }
    }

    void runPostReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(logger, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        hooksConfig.postReleaseHooks.each { it.act(context) }
    }
}
