package pl.allegro.tech.build.axion.release.domain.properties

import groovy.transform.Immutable
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction

@Immutable
class HooksProperties {

    final List<ReleaseHookAction> preReleaseHooks

    final List<ReleaseHookAction> postReleaseHooks

}
