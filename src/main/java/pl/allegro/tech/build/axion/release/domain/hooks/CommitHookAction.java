package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.function.BiFunction;

public class CommitHookAction implements ReleaseHookAction {

    private final Closure customAction;

    public CommitHookAction(Closure customAction) {
        this.customAction = customAction;
    }

    public CommitHookAction() {
        this(new Closure<String>(null) {
            @Override
            public String call(Object... args) {
                return "Release version: " + args[0];
            }
        });
    }

    @Override
    public void act(HookContext hookContext) {
        String message = (customAction.call(hookContext.getReleaseVersion(), hookContext.getPosition())).toString();
        hookContext.commit(hookContext.getPatternsToCommit(), message);
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create() {
            return new CommitHookAction();
        }

        @Override
        public ReleaseHookAction create(Closure customAction) {
            return new CommitHookAction(customAction);
        }

    }
}
