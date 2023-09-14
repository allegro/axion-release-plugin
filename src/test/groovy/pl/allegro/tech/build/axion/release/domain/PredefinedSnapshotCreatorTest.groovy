package pl.allegro.tech.build.axion.release.domain

import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class PredefinedSnapshotCreatorTest extends Specification {

    def "default snapshot creator should just return -SNAPSHOT"() {
        expect:
        PredefinedSnapshotCreator.SIMPLE.snapshotCreator.apply('version', scmPosition('master')) == '-SNAPSHOT'
    }

    def "unclean snapshot creator should just return -SNAPSHOT when scm is clean"() {
        expect:
        PredefinedSnapshotCreator.UNCLEAN.snapshotCreator.apply('version', scmPosition('master')) == '-SNAPSHOT'
    }

    def "unclean snapshot creator should return -unclean-SNAPSHOT when scm is NOT clean"() {
        expect:
        PredefinedSnapshotCreator.UNCLEAN.snapshotCreator.apply('version', scmPosition().withBranch('master').withUnclean().build()) == '-unclean-SNAPSHOT'
    }
}
