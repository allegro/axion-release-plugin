package pl.allegro.tech.build.axion.release.domain.hooks

import org.gradle.api.tasks.Input

class HooksConfig {

    @Input
    List<ReleaseHookAction> preReleaseHooks = []

    @Input
    List<ReleaseHookAction> postReleaseHooks = []

    void pre(Closure c) {
        preReleaseHooks.add(PredefinedReleaseHookAction.DEFAULT.factory.create(c))
    }

    void pre(String type) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create())
    }

    void pre(String type, Map arguments) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(arguments))
    }

    void pre(String type, Closure customAction) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(customAction))
    }

    void post(String type) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create())
    }

    void post(Closure c) {
        postReleaseHooks.add(PredefinedReleaseHookAction.DEFAULT.factory.create(c))
    }

    void post(String type, Map arguments) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(arguments))
    }

    void post(String type, Closure c) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(c))
    }
}
