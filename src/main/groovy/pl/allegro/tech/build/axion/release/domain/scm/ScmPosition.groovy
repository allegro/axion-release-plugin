package pl.allegro.tech.build.axion.release.domain.scm

class ScmPosition {

    final String branch

    final String latestTag

    final boolean onTag

    ScmPosition(String branch, String latestTag, boolean onTag) {
        this.branch = branch
        this.latestTag = latestTag
        this.onTag = onTag
    }

    boolean tagless() {
        return latestTag == null
    }


    @Override
    public String toString() {
        return "ScmPosition[branch = $branch, latestTag = $latestTag, onTag = $onTag]"
    }
}
