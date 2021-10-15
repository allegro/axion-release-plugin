package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input

class VersionScmPosition {

    @Input
    final String revision

    @Input
    final String shortRevision

    @Input
    final String branch

    VersionScmPosition(String revision, String shortRevision, String branch) {
        this.revision = revision
        this.shortRevision = shortRevision
        this.branch = branch
    }

    @Override
    public String toString() {
        return "VersionScmPosition[revision = $revision, shortRevision = $shortRevision, branch = $branch]"
    }

}
