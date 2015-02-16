package pl.allegro.tech.build.axion.release.domain.hooks

interface ReleaseHookAction {
    
    void act(HookContext hookContext)
    
}
