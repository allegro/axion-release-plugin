package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class PredefinedVersionCreatorTest extends Specification {

    def "default version creator should just return version string"() {
        expect:
        PredefinedVersionCreator.SIMPLE.versionCreator('version', new ScmPosition('master')) == 'version'
    }

    def "versionWithBranch version creator should return simple version when on master"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator('version', new ScmPosition('master')) == 'version'
    }

    def "versionWithBranch version creator should return version with appended branch name when not on master"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator('version', new ScmPosition('branch')) == 'version-branch'
    }

    def "should return version creator of given type"() {
        expect:
        PredefinedVersionCreator.versionCreatorFor('simple')('version', null) == 'version'
    }

    def "should throw exception when trying to obtain undefined version creator"() {
        when:
        PredefinedVersionCreator.versionCreatorFor('unknown')

        then:
        thrown(IllegalArgumentException)
    }
}
