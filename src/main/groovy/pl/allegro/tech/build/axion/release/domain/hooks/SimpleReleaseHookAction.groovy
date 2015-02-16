package pl.allegro.tech.build.axion.release.domain.hooks

class SimpleReleaseHookAction implements ReleaseHookAction {

    private final Closure customAction

    SimpleReleaseHookAction(Closure customAction) {
        this.customAction = customAction
    }

    @Override
    void act(HookContext hookContext) {
        customAction(hookContext)
    }

    static final class Factory extends DefaultReleaseHookFactory {

        @Override
        ReleaseHookAction create(Closure customAction) {
            return new SimpleReleaseHookAction(customAction)
        }
    }
}
