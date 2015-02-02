package pl.allegro.tech.build.axion.release.domain.hooks

class HooksConfig {
    
    List<ReleaseHook> preReleaseHooks = []
    
    List<ReleaseHook> postReleaseHooks = []
    
    void pre(Closure c) {
        preReleaseHooks.add(c)
    }

    void pre(String type, Map arguments = [:]) {
        preReleaseHooks.add(c)
    }

    void post(Closure c) {
        postReleaseHooks.add(c)
    }
    
    void post(String type, Closure c) {
        postReleaseHooks.add(c)
    }
}
