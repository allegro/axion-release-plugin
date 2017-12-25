package pl.allegro.tech.build.axion.release.domain.scm

class ScmPushResult {

    final boolean success

    final Optional<String> remoteMessage

    ScmPushResult(boolean success, Optional<String> remoteMessage) {
        this.success = success
        this.remoteMessage = remoteMessage
    }
}
