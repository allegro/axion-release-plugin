package pl.allegro.tech.build.axion.release.domain.hooks

interface ReleaseHookFactory {
    
    ReleaseHook create()
    
    ReleaseHook create(Map arguments)
    
    ReleaseHook create(Closure customAction)
}
