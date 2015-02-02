package pl.allegro.tech.build.axion.release.domain.hooks

class SimpleReleaseHook implements ReleaseHook {

    @Override
    void act(HookContext hookContext, Map arguments, Closure customAction) {
        customAction(hookContext)
    }
}
