package pl.allegro.tech.build.axion.release.domain.scm

class ScmPosition {

    final String revision

    final String shortRevision

    final String branch

    ScmPosition(String revision, String shortRevision, String branch) {
        this.revision = revision
        this.shortRevision = shortRevision
        this.branch = branch
    }

    @Override
    public String toString() {
        return "ScmPosition[revision = $revision, shortRevision = $shortRevision, branch = $branch]"
    }
}
