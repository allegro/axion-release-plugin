package pl.allegro.tech.build.axion.release.domain.hooks

class CommitHook implements ReleaseHook {
    
    @Override
    void act(HookContext hookContext, Map arguments, Closure customAction) {
        hookContext.commit(hookContext.patternsToCommit, customAction(hookContext.currentVersion, hookContext.position))
    }
}
