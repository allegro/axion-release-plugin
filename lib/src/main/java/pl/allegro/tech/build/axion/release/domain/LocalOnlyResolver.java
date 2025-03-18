package pl.allegro.tech.build.axion.release.domain;

public class LocalOnlyResolver {

    private final boolean localOnly;

    public LocalOnlyResolver(boolean localOnly) {
        this.localOnly = localOnly;
    }

    public boolean localOnly(boolean remoteAttached) {
        return localOnly || !remoteAttached;
    }
}
