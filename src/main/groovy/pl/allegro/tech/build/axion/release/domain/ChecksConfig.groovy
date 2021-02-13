package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input

class ChecksConfig {

    @Input
    boolean aheadOfRemote = true

    @Input
    boolean uncommittedChanges = true

    @Input
    boolean snapshotDependencies = true

}
