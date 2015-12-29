package pl.allegro.tech.build.axion.release.domain.scm

class ScmProperties {

    final String type

    final File directory

    final String remote

    final boolean pushTagsOnly

    final boolean fetchTags

    final boolean attachRemote

    final String remoteUrl

    final ScmIdentity identity

    ScmProperties(String type, File directory, String remote, boolean pushTagsOnly,
                  boolean fetchTags, boolean attachRemote, String remoteUrl,
                  ScmIdentity identity) {
        this.type = type
        this.directory = directory
        this.remote = remote
        this.pushTagsOnly = pushTagsOnly
        this.fetchTags = fetchTags
        this.attachRemote = attachRemote
        this.remoteUrl = remoteUrl
        this.identity = identity
    }

    ScmPushOptions pushOptions() {
        return new ScmPushOptions(remote: remote, pushTagsOnly: pushTagsOnly)
    }
}
