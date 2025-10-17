package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class PredefinedSnapshotCreatorTest extends Specification {

    def "default snapshot creator should just return -SNAPSHOT"() {
        given:
        def versionContext = versionContext(scmPosition('master'))

        expect:
        PredefinedSnapshotCreator.SIMPLE.snapshotCreator.apply('0.1.0', versionContext) == '-SNAPSHOT'
    }

    def "unclean snapshot creator should just return -SNAPSHOT when scm is clean"() {
        given:
        def versionContext = versionContext(scmPosition('master'))

        expect:
        PredefinedSnapshotCreator.UNCLEAN.snapshotCreator.apply('0.1.0', versionContext) == '-SNAPSHOT'
    }

    def "unclean snapshot creator should return -unclean-SNAPSHOT when scm is NOT clean"() {
        given:
        def versionContext = versionContext(scmPosition().withBranch('master').withUnclean().build())

        expect:
        PredefinedSnapshotCreator.UNCLEAN.snapshotCreator.apply('0.1.0', versionContext) == '-unclean-SNAPSHOT'
    }

    private VersionContext versionContext(ScmPosition position) {
        return new VersionContext(
            Version.parse('0.1.0'),
            true,
            Version.parse('0.1.0'),
            position
        )
    }
}
