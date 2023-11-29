package pl.allegro.tech.build.axion.release.domain.scm;

import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.util.Optional;

public class ScmPushResult {

    private final boolean success;

    private final Optional<RemoteRefUpdate.Status> failureCause;

    private final Optional<String> remoteMessage;

    public ScmPushResult(boolean success, Optional<RemoteRefUpdate.Status> failureCause, Optional<String> remoteMessage) {
        this.success = success;
        this.failureCause = failureCause;
        this.remoteMessage = remoteMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<RemoteRefUpdate.Status> getFailureStatus() {
        return failureCause;
    }

    public Optional<String> getRemoteMessage() {
        return remoteMessage;
    }
}
