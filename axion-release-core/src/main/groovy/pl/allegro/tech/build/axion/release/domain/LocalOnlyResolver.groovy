package pl.allegro.tech.build.axion.release.domain

class LocalOnlyResolver {

    private final boolean localOnly

    LocalOnlyResolver(boolean baseValue) {
        this.localOnly = baseValue
    }

    boolean localOnly(boolean remoteAttached) {
        return localOnly || !remoteAttached
    }
}
