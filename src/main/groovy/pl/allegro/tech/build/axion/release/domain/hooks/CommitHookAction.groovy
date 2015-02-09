package pl.allegro.tech.build.axion.release.domain.hooks

class CommitHookAction implements ReleaseHookAction {
    
    private Closure customAction

    CommitHookAction(Closure customAction) {
        this.customAction = customAction
    }

    CommitHookAction() {
        this({v, p -> "Release version: $v"})
    }
    
    @Override
    void act(HookContext hookContext) {
        String message = customAction(hookContext.currentVersion, hookContext.position)
        hookContext.commit(hookContext.patternsToCommit, message)
    }

    static final class Factory extends DefaultReleaseHookFactory {

        @Override
        ReleaseHookAction create() {
            return new CommitHookAction()
        }

        @Override
        ReleaseHookAction create(Closure customAction) {
            return new CommitHookAction(customAction)
        }
    }
}
