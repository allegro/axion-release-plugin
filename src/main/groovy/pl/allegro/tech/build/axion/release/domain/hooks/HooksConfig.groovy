package pl.allegro.tech.build.axion.release.domain.hooks

class HooksConfig {
    
    List<ReleaseHook> preReleaseHooks = []
    
    List<ReleaseHook> postReleaseHooks = []
    
    void pre(Closure c) {
        preReleaseHooks.add(PredefinedReleaseHook.DEFAULT.factory.create(c))
    }

    void pre(String type) {
        postReleaseHooks.add(PredefinedReleaseHook.factoryFor(type).create())
    }
    
    void pre(String type, Map arguments) {
        preReleaseHooks.add(PredefinedReleaseHook.factoryFor(type).create(arguments))
    }

    void pre(String type, Closure customAction) {
        preReleaseHooks.add(PredefinedReleaseHook.factoryFor(type).create(customAction))
    }

    void post(Closure c) {
        postReleaseHooks.add(PredefinedReleaseHook.DEFAULT.factory.create(c))
    }

    void post(String type, Map arguments) {
        postReleaseHooks.add(PredefinedReleaseHook.factoryFor(type).create(arguments))
    }
    
    void post(String type, Closure c) {
        postReleaseHooks.add(PredefinedReleaseHook.factoryFor(type).create(c))
    }
}
