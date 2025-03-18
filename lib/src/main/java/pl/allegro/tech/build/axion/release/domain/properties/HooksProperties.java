package pl.allegro.tech.build.axion.release.domain.properties;

import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HooksProperties {

    private final List<ReleaseHookAction> preReleaseHooks;
    private final List<ReleaseHookAction> postReleaseHooks;

    public HooksProperties(List<ReleaseHookAction> preReleaseHooks, List<ReleaseHookAction> postReleaseHooks) {
        this.preReleaseHooks = new ArrayList<>(preReleaseHooks);
        this.postReleaseHooks = new ArrayList<>(postReleaseHooks);
    }

    public final List<ReleaseHookAction> getPreReleaseHooks() {
        return Collections.unmodifiableList(preReleaseHooks);
    }

    public final List<ReleaseHookAction> getPostReleaseHooks() {
        return Collections.unmodifiableList(postReleaseHooks);
    }
}
