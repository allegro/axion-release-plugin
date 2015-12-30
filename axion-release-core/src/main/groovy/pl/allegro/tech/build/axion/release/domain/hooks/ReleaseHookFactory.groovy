package pl.allegro.tech.build.axion.release.domain.hooks

interface ReleaseHookFactory {
    
    ReleaseHookAction create()
    
    ReleaseHookAction create(Map arguments)
    
    ReleaseHookAction create(Closure customAction)
}
