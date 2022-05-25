package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.PredefinedReleaseCommitMessageCreator
import pl.allegro.tech.build.axion.release.domain.hooks.PredefinedReleaseHookAction
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction

class HooksPropertiesBuilder {

    private final List<ReleaseHookAction> pre = []

    private final List<ReleaseHookAction> post = []

    private HooksPropertiesBuilder() {
    }

    static HooksPropertiesBuilder hooksProperties() {
        return new HooksPropertiesBuilder()
    }

    HooksProperties build() {
        return new HooksProperties(pre, post)
    }

    HooksPropertiesBuilder withCommitHook() {
        pre.add(PredefinedReleaseHookAction.factoryFor('commit').create(
            { hookContext -> PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator.apply(hookContext.currentVersion, hookContext.position) }
        ))
        return this
    }
}
