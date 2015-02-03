package pl.allegro.tech.build.axion.release.domain.hooks

enum PredefinedReleaseHook {

    DEFAULT('default', new SimpleReleaseHook.Factory()),
    FILE_UPDATE('fileUpdate', new FileUpdateHook.Factory()),
    COMMIT('commit', new CommitHook.Factory());

    private final String type

    final ReleaseHookFactory factory

    private PredefinedReleaseHook(String type, ReleaseHookFactory releaseHook) {
        this.type = type
        this.factory = releaseHook
    }

    static ReleaseHookFactory factoryFor(String type) {
        PredefinedReleaseHook factory = values().find { it.type == type }
        if (factory == null) {
            throw new IllegalArgumentException("There is no predefined release hook with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return factory.factory
    }
    
}
