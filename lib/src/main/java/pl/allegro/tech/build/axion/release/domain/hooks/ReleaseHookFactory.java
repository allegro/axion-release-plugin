package pl.allegro.tech.build.axion.release.domain.hooks;

import java.util.Map;

public interface ReleaseHookFactory {

    interface CustomAction {
        Object apply(HookContext hookContext);
    }

    ReleaseHookAction create();

    ReleaseHookAction create(Map<String, Object> arguments);

    ReleaseHookAction create(CustomAction customAction);
}
