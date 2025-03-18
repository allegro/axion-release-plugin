package pl.allegro.tech.build.axion.release.domain.hooks;

import java.util.Arrays;
import java.util.Optional;

public enum PredefinedReleaseHookAction {

    DEFAULT("default", new SimpleReleaseHookAction.Factory()),
    FILE_UPDATE("fileUpdate", new FileUpdateHookAction.Factory()),
    COMMIT("commit", new CommitHookAction.Factory()),
    PUSH("push", new PushHookAction.Factory());

    private final String type;
    private final ReleaseHookFactory factory;

    PredefinedReleaseHookAction(String type, ReleaseHookFactory releaseHook) {
        this.type = type;
        this.factory = releaseHook;
    }

    public static ReleaseHookFactory factoryFor(final String type) {
        Optional<PredefinedReleaseHookAction> factory = Arrays.stream(values())
            .filter(it -> it.type.equals(type))
            .findFirst();

        return factory.orElseThrow(
            () -> new IllegalArgumentException(
                "There is no predefined release hook with " + type + " type. You can choose from: " + values()
            )
        ).getFactory();

    }

    public final ReleaseHookFactory getFactory() {
        return factory;
    }
}
