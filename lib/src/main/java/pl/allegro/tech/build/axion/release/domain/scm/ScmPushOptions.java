package pl.allegro.tech.build.axion.release.domain.scm;

public class ScmPushOptions {

    private final String remote;

    private final boolean pushTagsOnly;

    public ScmPushOptions(String remote, boolean pushTagsOnly) {
        this.remote = remote;
        this.pushTagsOnly = pushTagsOnly;
    }

    public String getRemote() {
        return remote;
    }

    public boolean isPushTagsOnly() {
        return pushTagsOnly;
    }
}
