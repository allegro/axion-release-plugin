package pl.allegro.tech.build.axion.release.domain.hooks

interface ReleaseHook {
    
    void act(HookContext hookContext, Map arguments, Closure customAction)
    
}
