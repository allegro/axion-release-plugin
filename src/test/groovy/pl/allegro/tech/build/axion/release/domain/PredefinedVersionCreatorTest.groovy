package pl.allegro.tech.build.axion.release.domain


import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class PredefinedVersionCreatorTest extends Specification {

    def "default version creator should just return version string"() {
        expect:
        PredefinedVersionCreator.SIMPLE.versionCreator.apply('version',scmPosition('master')) == 'version'
    }

    def "versionWithBranch should not append on release branch"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator.apply('version', scmPosition().withBranch('release').asReleaseBranch().build()) == 'version'
    }

    def "versionWithBranch should append branch name when not on release branch"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator.apply('version', scmPosition('feature/branch')) == 'version-feature/branch'
    }

    def "versionWithCommitHash should not append on release branch"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_COMMIT_HASH.versionCreator.apply('version', scmPosition().withBranch('release').asReleaseBranch().build()) == 'version'
    }

    def "versionWithCommitHash should append short SHA-1 hash when not on release branch"() {
        expect:
        PredefinedVersionCreator.VERSION_WITH_COMMIT_HASH.versionCreator.apply('version', scmPosition('branch')) == 'version-c143976'
    }

    def "VERSION_WITH_BRANCH should not append when ref is a version tag"() {
        given:
        def pos = scmPosition().withBranch('refs/tags/v1.0.0').build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator.apply('version', pos) == 'version'
    }

    def "VERSION_WITH_BRANCH should not append when ref is a random tag"() {
        given:
        def pos = scmPosition().withBranch('refs/tags/random-tag').build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator.apply('version', pos) == 'version-refs/tags/random-tag'
    }

    def "VERSION_WITH_COMMIT_HASH should append when ref is a random tag"() {
        given:
        def pos = scmPosition().withBranch('refs/tags/random-tag').build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_COMMIT_HASH.versionCreator.apply('version', pos) == 'version-c143976'
    }

    def "VERSION_WITH_COMMIT_HASH should not append when ref is a version tag"() {
        given:
        def pos = scmPosition().withBranch('refs/tags/v1.0.0').build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_COMMIT_HASH.versionCreator.apply('version', pos) == 'version'
    }

    def "VERSION_WITH_BRANCH should not append branch name when isTagRef is true (HEAD on tag, but on branch)"() {
        given:
        def pos = scmPosition().withBranch('feature/branch').withTagRef().build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_BRANCH.versionCreator.apply('version', pos) == 'version'
    }

    def "VERSION_WITH_COMMIT_HASH should not append hash when isTagRef is true (HEAD on tag, but on branch)"() {
        given:
        def pos = scmPosition().withBranch('feature/branch').withTagRef().build()
        expect:
        PredefinedVersionCreator.VERSION_WITH_COMMIT_HASH.versionCreator.apply('version', pos) == 'version'
    }

    def "should return version creator of given type"() {
        expect:
        PredefinedVersionCreator.versionCreatorFor('simple').apply('version', null) == 'version'
    }

    def "should throw exception when trying to obtain undefined version creator"() {
        when:
        PredefinedVersionCreator.versionCreatorFor('unknown')

        then:
        thrown(IllegalArgumentException)
    }
}
