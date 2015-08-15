package pl.allegro.tech.build.axion.release.domain.hooks

enum PredefinedReleaseHookAction {

    DEFAULT('default', new SimpleReleaseHookAction.Factory()),
    FILE_UPDATE('fileUpdate', new FileUpdateHookAction.Factory()),
    COMMIT('commit', new CommitHookAction.Factory()),
    PUSH('push', new PushHookAction.Factory());

    private final String type

    final ReleaseHookFactory factory

    private PredefinedReleaseHookAction(String type, ReleaseHookFactory releaseHook) {
        this.type = type
        this.factory = releaseHook
    }

    static ReleaseHookFactory factoryFor(String type) {
        PredefinedReleaseHookAction factory = values().find { it.type == type }
        if (factory == null) {
            throw new IllegalArgumentException("There is no predefined release hook with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return factory.factory
    }
    
}
