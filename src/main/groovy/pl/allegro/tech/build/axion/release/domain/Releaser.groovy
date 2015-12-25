package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class Releaser {

    private final ReleaseLogger logger = ReleaseLogger.Factory.logger(Releaser)

    private final VersionService versionService

    private final ScmService repository

    private final ReleaseHooksRunner hooksRunner

    Releaser(VersionService versionService, ScmService repository, ReleaseHooksRunner hooksRunner) {
        this.versionService = versionService
        this.repository = repository
        this.hooksRunner = hooksRunner
    }

    void release(Properties rules) {
        VersionWithPosition positionedVersion = versionService.currentVersion(rules.version, rules.tag, rules.nextVersion)
        Version version = positionedVersion.version

        if (notOnTagAlready(positionedVersion) || rules.version.forceVersion()) {
            String tagName = rules.tag.serialize(rules.tag, version.toString())

            hooksRunner.runPreReleaseHooks(rules.hooks, rules, positionedVersion, version)

            logger.quiet("Creating tag: $tagName")
            repository.tag(tagName)

            hooksRunner.runPostReleaseHooks(rules.hooks, rules, positionedVersion, version)
        } else {
            logger.quiet("Working on released version ${version}, nothing to release.")
        }
    }

    void pushRelease() {
        repository.push()
    }

    private boolean notOnTagAlready(VersionWithPosition positionedVersion) {
        return positionedVersion.snapshotVersion
    }
}
