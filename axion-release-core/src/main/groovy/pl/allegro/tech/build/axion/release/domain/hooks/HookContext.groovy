package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class HookContext {
    
    static final ReleaseLogger logger = ReleaseLogger.Factory.logger(HookContext.class)

    private final VersionService versionService

    private final ScmService scmService
    
    final ScmPosition position
    
    private String previousVersion
    
    private String currentVersion
    
    private final Properties rules
    
    final List<String> patternsToCommit = []
    
    HookContext(Properties rules, VersionService versionService, ScmService scmService,
                ScmPosition position, Version previousVersion, Version currentVersion) {
        this.versionService = versionService
        this.rules = rules
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
        String version = versionService.currentDecoratedVersion(rules.version, rules.tag, rules.nextVersion)
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
