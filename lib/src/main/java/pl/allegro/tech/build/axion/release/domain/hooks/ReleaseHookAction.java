package pl.allegro.tech.build.axion.release.domain.hooks;

@FunctionalInterface
public interface ReleaseHookAction {

    void act(HookContext hookContext);
}
