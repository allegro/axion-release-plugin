package pl.allegro.tech.build.axion.release.domain

import java.util.function.BooleanSupplier

class ScmStateBuilder {

    private boolean onReleaseTag = false

    private boolean onNextVersionTag = false

    private boolean noReleaseTagsFound = false

    private boolean hasUncommittedChanges = false

    static ScmStateBuilder scmState() {
        return new ScmStateBuilder()
    }

    ScmState build() {
        return new ScmState(onReleaseTag, onNextVersionTag, noReleaseTagsFound, { hasUncommittedChanges } as BooleanSupplier)
    }

    ScmStateBuilder onReleaseTag() {
        this.onReleaseTag = true
        return this
    }

    ScmStateBuilder onNextVersionTag() {
        this.onNextVersionTag = true
        return this
    }

    ScmStateBuilder noReleaseTagsFound() {
        this.noReleaseTagsFound = true
        return this
    }

    ScmStateBuilder hasUncomittedChanges() {
        this.hasUncommittedChanges = true
        return this
    }
}
