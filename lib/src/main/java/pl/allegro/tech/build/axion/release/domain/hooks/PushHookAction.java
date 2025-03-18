package pl.allegro.tech.build.axion.release.domain.hooks;

public class PushHookAction implements ReleaseHookAction {

    @Override
    public void act(HookContext hookContext) {
        hookContext.push();
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create() {
            return new PushHookAction();
        }

    }
}
