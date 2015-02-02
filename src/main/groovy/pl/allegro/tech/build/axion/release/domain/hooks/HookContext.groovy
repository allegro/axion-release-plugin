package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class HookContext {
    
    private final ScmService scmService
    
    final ScmPosition position
    
    final String previousVersion
    
    final String currentVersion
    
    final List<String> patternsToCommit = []
    
    HookContext(ScmService scmService, ScmPosition position, Version previousVersion, Version currentVersion) {
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
