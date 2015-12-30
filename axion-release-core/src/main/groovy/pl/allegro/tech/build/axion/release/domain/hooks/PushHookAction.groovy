package pl.allegro.tech.build.axion.release.domain.hooks

class PushHookAction implements ReleaseHookAction {

    @Override
    void act(HookContext hookContext) {
        hookContext.push()
    }

    static final class Factory extends DefaultReleaseHookFactory {

        @Override
        ReleaseHookAction create() {
            return new PushHookAction()
        }

        @Override
        ReleaseHookAction create(Closure customAction) {
            return create()
        }
    }
}
