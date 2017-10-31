package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.VersionContext
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.properties.HooksProperties
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class ReleaseHooksRunner {

    private final VersionService versionService

    private final ScmService scmService

    ReleaseHooksRunner(VersionService versionService, ScmService scmService) {
        this.versionService = versionService
        this.scmService = scmService
    }

    void runPreReleaseHooks(HooksProperties hooksRules, Properties rules, VersionContext versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(rules, versionService, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        hooksRules.preReleaseHooks.each { it.act(context) }
    }

    void runPostReleaseHooks(HooksProperties hooksRules, Properties rules, VersionContext versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(rules, versionService, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        hooksRules.postReleaseHooks.each { it.act(context) }
    }
}
