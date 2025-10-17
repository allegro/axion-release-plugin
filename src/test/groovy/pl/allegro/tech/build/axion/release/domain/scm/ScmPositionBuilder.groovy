package pl.allegro.tech.build.axion.release.domain.scm

class ScmPositionBuilder {

    private String branch = 'master'

    private String revision = 'c1439767113643abda121896ee3fa42b500f16d0'

    private String shortRevision = 'c143976'

    private boolean isClean = true

    private boolean isReleaseBranch = false

    private ScmPositionBuilder() {
    }

    static ScmPositionBuilder scmPosition() {
        return new ScmPositionBuilder()
    }

    static ScmPosition scmPosition(String branch) {
        return new ScmPositionBuilder().withBranch(branch).build()
    }

    ScmPosition build() {
        return new ScmPosition(revision, shortRevision, branch, isClean, isReleaseBranch)
    }

    ScmPositionBuilder withBranch(String branch) {
        this.branch = branch
        return this
    }

    ScmPositionBuilder withRevision(String revision) {
        this.revision = revision
        this.shortRevision = revision.substring(0, 7)
        return this
    }

    ScmPositionBuilder withUnclean() {
        this.isClean = false
        return this
    }

    ScmPositionBuilder asReleaseBranch() {
        this.isReleaseBranch = true
        return this
    }
}
