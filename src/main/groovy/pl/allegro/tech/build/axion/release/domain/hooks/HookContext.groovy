package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class HookContext {
    
    final Logger logger
    
    private final ScmService scmService
    
    final ScmPosition position
    
    final String previousVersion
    
    final String currentVersion
    
    final List<String> patternsToCommit = []
    
    HookContext(Logger logger, ScmService scmService, 
                ScmPosition position, Version previousVersion, Version currentVersion) {
        this.logger = logger
        this.scmService = scmService
        this.position = position
        this.previousVersion = previousVersion.toString()
        this.currentVersion = currentVersion.toString()
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
}
