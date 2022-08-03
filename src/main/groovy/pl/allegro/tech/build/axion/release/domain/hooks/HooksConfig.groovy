package pl.allegro.tech.build.axion.release.domain.hooks

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import pl.allegro.tech.build.axion.release.domain.BaseExtension
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookFactory.CustomAction

abstract class HooksConfig extends BaseExtension {

    @Internal
    abstract ListProperty<ReleaseHookAction> getPreReleaseHooks()

    @Internal
    abstract ListProperty<ReleaseHookAction> getPostReleaseHooks()

    void pre(ReleaseHookAction action) {
        pre({HookContext c -> action.act(c)})
    }

    void pre(Closure c) {
        preReleaseHooks.add(PredefinedReleaseHookAction.DEFAULT.factory.create(safeCastToCustomAction(c)))
    }

    void pre(String type) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create())
    }

    void pre(String type, Map arguments) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(arguments))
    }

    void pre(String type, ReleaseHookAction action) {
        pre(type, {HookContext c -> action.act(c)})
    }

    void pre(String type, Closure c) {
        preReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(safeCastToCustomAction(c)))
    }

    void post(String type) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create())
    }

    void post(ReleaseHookAction action) {
        post({HookContext c -> action.act(c)})
    }

    void post(Closure c) {
        postReleaseHooks.add(PredefinedReleaseHookAction.DEFAULT.factory.create(safeCastToCustomAction(c)))
    }

    void post(String type, Map arguments) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(arguments))
    }

    void post(String type, ReleaseHookAction action) {
        post(type, {HookContext c -> action.act(c)})
    }

    void post(String type, Closure c) {
        postReleaseHooks.add(PredefinedReleaseHookAction.factoryFor(type).create(safeCastToCustomAction(c)))
    }

    static CustomAction safeCastToCustomAction(Closure closure) {
        return closure.parameterTypes.length == 1 && closure.parameterTypes[0].isAssignableFrom(HookContext.class)
            ? closure
            : { HookContext hookContext -> closure(hookContext.releaseVersion, hookContext.position)}
    }
}
