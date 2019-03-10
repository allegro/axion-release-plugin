package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;

public class SimpleReleaseHookAction implements ReleaseHookAction {

    private final Closure customAction;

    public SimpleReleaseHookAction(Closure customAction) {
        this.customAction = customAction;
    }

    @Override
    public void act(HookContext hookContext) {
        customAction.call(hookContext);
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create(Closure customAction) {
            return new SimpleReleaseHookAction(customAction);
        }

    }
}
