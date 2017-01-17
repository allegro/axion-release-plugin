package pl.allegro.tech.build.axion.release.domain.scm

class ScmPosition {

    final String branch

    ScmPosition(String branch) {
        this.branch = branch
    }

    @Override
    public String toString() {
        return "ScmPosition[branch = $branch]"
    }
}
