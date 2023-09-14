package pl.allegro.tech.build.axion.release.infrastructure.git

import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit
import pl.allegro.tech.build.axion.release.infrastructure.NoOpRepository
import spock.lang.Specification

class NoOpRepositoryTest extends Specification {

    ScmRepository scm = Mock(ScmRepository)

    NoOpRepository dryRepository = new NoOpRepository(scm)

    def "should not create actual tags in scm"() {
        when:
        dryRepository.tag("dry_tag")

        then:
        0 * scm.tag(_)
    }

    def "should not push anything to scm"() {
        when:
        dryRepository.push(ScmIdentity.defaultIdentityWithoutAgents(), new ScmPushOptions('dry-remote', false))

        then:
        0 * scm.push(_, _)
    }

    def "should not commit anything to scm"() {
        when:
        dryRepository.commit(['*'], "yo")

        then:
        0 * scm.commit(_)
    }

    def "should return latest tags from real scm"() {
        given:
        TagsOnCommit expected = new TagsOnCommit('commit-id', [])
        scm.latestTags(_) >> expected

        when:
        TagsOnCommit current = dryRepository.latestTags(~/^release.*/)

        then:
        current == expected
    }

    def "should check uncommitted changes on real scm"() {
        given:
        scm.checkUncommittedChanges() >> true

        expect:
        dryRepository.checkUncommittedChanges()
    }

    def "should check ahead of remote on real scm"() {
        given:
        scm.checkAheadOfRemote() >> true

        expect:
        dryRepository.checkAheadOfRemote()
    }

}
