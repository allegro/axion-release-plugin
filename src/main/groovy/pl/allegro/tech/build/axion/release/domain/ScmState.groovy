package pl.allegro.tech.build.axion.release.domain

class ScmState {

    final boolean onReleaseTag

    final boolean onNextVersionTag

    final boolean noReleaseTagsFound

    final boolean hasUncommittedChanges

    ScmState(boolean onReleaseTag, boolean onNextVersionTag, boolean noReleaseTagsFound, boolean hasUncommittedChanges) {
        this.onReleaseTag = onReleaseTag
        this.onNextVersionTag = onNextVersionTag
        this.noReleaseTagsFound = noReleaseTagsFound
        this.hasUncommittedChanges = hasUncommittedChanges
    }
}
