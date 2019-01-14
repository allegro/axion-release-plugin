package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.Optional;

public class ScmPushResult {

    private final boolean success;
    private final Optional<String> remoteMessage;

    public ScmPushResult(boolean success, Optional<String> remoteMessage) {
        this.success = success;
        this.remoteMessage = remoteMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<String> getRemoteMessage() {
        return remoteMessage;
    }
}
