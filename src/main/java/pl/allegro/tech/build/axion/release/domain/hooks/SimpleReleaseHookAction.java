package pl.allegro.tech.build.axion.release.domain.hooks;

import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookFactory.CustomAction;

public class SimpleReleaseHookAction implements ReleaseHookAction {

    private final CustomAction customAction;

    public SimpleReleaseHookAction(CustomAction customAction) {
        this.customAction = customAction;
    }

    @Override
    public void act(HookContext hookContext) {
        customAction.apply(hookContext);
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create(CustomAction customAction) {
            return new SimpleReleaseHookAction(customAction);
        }

    }
}
