package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class HookContext {
    
    final Logger logger
    
    private final ScmService scmService
    
    final ScmPosition position
    
    private String previousVersion
    
    private String currentVersion
    
    private final VersionConfig versionConfig
    
    final List<String> patternsToCommit = []
    
    HookContext(Logger logger, VersionConfig versionConfig, ScmService scmService,
                ScmPosition position, Version previousVersion, Version currentVersion) {
        this.logger = logger
        this.versionConfig = versionConfig
        this.scmService = scmService
        this.position = position
        this.previousVersion = previousVersion.toString()
        this.currentVersion = currentVersion.toString()
    }

    String getReleaseVersion() {
        return currentVersion
    }
    
    @Deprecated
    String getCurrentVersion() {
        return currentVersion
    }

    String getPreviousVersion() {
        return previousVersion
    }
    
    String readVersion() {
        String version = versionConfig.uncachedVersion
        logger.info("Read version $version from repository without using cache")
        return version
    }
    
    void commit(List patterns, String message) {
        scmService.commit(patterns, message)
    }
    
    void addCommitPattern(String pattern) {
        patternsToCommit.add(pattern)
    }

    void addCommitPattern(Collection patterns) {
        patternsToCommit.addAll(patterns)
    }

    void push() {
        scmService.push()
    }
}
