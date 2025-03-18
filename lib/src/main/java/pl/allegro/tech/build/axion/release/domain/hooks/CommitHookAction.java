package pl.allegro.tech.build.axion.release.domain.hooks;

import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookFactory.CustomAction;

public class CommitHookAction implements ReleaseHookAction {

    private final CustomAction customAction;

    public CommitHookAction(CustomAction customAction) {
        this.customAction = customAction;
    }

    public CommitHookAction() {
        this((hookContext) -> "Release version: " + hookContext.getCurrentVersion());
    }

    @Override
    public void act(HookContext hookContext) {
        String message = (customAction.apply(hookContext)).toString();
        hookContext.commit(hookContext.getPatternsToCommit(), message);
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create() {
            return new CommitHookAction();
        }

        @Override
        public ReleaseHookAction create(CustomAction customAction) {
            return new CommitHookAction(customAction);
        }

    }
}
