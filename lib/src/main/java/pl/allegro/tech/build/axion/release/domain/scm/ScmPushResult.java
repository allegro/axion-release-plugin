package pl.allegro.tech.build.axion.release.domain.scm;

import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.util.Optional;

public class ScmPushResult {

    private final ScmPushResultOutcome outcome;

    private final Optional<RemoteRefUpdate.Status> failureCause;

    private final Optional<String> remoteMessage;

    public ScmPushResult(ScmPushResultOutcome outcome,
                         Optional<RemoteRefUpdate.Status> failureCause,
                         Optional<String> remoteMessage) {
        this.outcome = outcome;
        this.failureCause = failureCause;
        this.remoteMessage = remoteMessage;
    }

    public ScmPushResultOutcome getOutcome() {
        return outcome;
    }

    public Optional<RemoteRefUpdate.Status> getFailureStatus() {
        return failureCause;
    }

    public Optional<String> getRemoteMessage() {
        return remoteMessage;
    }
}
