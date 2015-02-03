package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class ReleaseHooksRunner {
    
    private final ScmService scmService

    private final HooksConfig hooksConfig
    
    ReleaseHooksRunner(ScmService scmService, HooksConfig hooksConfig) {
        this.scmService = scmService
        this.hooksConfig = hooksConfig
    }

    void runPreReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(scmService, versionWithPosition.position, versionWithPosition.version, releaseVersion)
        hooksConfig.preReleaseHooks.each { it.act(context) }
    }

    void runPostReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(scmService, versionWithPosition.position, versionWithPosition.version, releaseVersion)
        hooksConfig.postReleaseHooks.each { it.act(context) }
    }
}
