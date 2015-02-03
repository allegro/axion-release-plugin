package pl.allegro.tech.build.axion.release.domain.hooks

class SimpleReleaseHook implements ReleaseHook {

    private final Closure customAction

    SimpleReleaseHook(Closure customAction) {
        this.customAction = customAction
    }

    @Override
    void act(HookContext hookContext) {
        customAction(hookContext)
    }

    static final class Factory extends DefaultReleaseHookFactory {

        @Override
        ReleaseHook create(Closure customAction) {
            return new SimpleReleaseHook(customAction)
        }
    }
}
