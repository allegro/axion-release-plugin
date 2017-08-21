package pl.allegro.tech.build.axion.release.domain.scm

import spock.lang.Specification

import java.util.regex.Pattern

class TagsOnCommitTest extends Specification {

    def "should return true when all tags match pattern when asking for hasOnlyMatching"() {
        given:
        TagsOnCommit tags = new TagsOnCommit('123', ['release-1.0.0', 'release-2.0.0'], false)

        expect:
        tags.hasOnlyMatching(Pattern.compile('release.*'))
    }

    def "should return false when not all tags match pattern when asking for hasOnlyMatching"() {
        given:
        TagsOnCommit tags = new TagsOnCommit('123', ['release-1.0.0', 'other-2.0.0'], false)

        expect:
        !tags.hasOnlyMatching(Pattern.compile('release.*'))
    }

    def "should return false when there are no tags when asking for hasOnlyMatching"() {
        given:
        TagsOnCommit tags = new TagsOnCommit('123', [], false)

        expect:
        !tags.hasOnlyMatching(Pattern.compile('release.*'))
    }

    def "should return true when there is at least one tag matching when asking for hasAnymatching"() {
        given:
        TagsOnCommit tags = new TagsOnCommit('123', ['release-1.0.0', 'other-2.0.0'], false)

        expect:
        tags.hasAnyMatching(Pattern.compile('release.*'))
    }

    def "should return false when there none matching when asking for hasAnymatching"() {
        given:
        TagsOnCommit tags = new TagsOnCommit('123', ['other-1.0.0', 'other-2.0.0'], false)

        expect:
        !tags.hasAnyMatching(Pattern.compile('release.*'))
    }
}
