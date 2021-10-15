package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*

/**
 *  Variant matrix that has to be tested:
 *  * on head / not on head
 *  * commits on same commit / on different commits
 *
 *  Plus ignoring next version:
 *  * ignore next version
 */
class VersionSorterTest extends Specification {

    private final VersionSorter sorter = new VersionSorter()

    private final VersionFactory factory = new VersionFactory(
        VersionPropertiesBuilder.versionProperties().build(),
        TagPropertiesBuilder.tagProperties().build(),
        NextVersionPropertiesBuilder.nextVersionProperties().build(),
        ScmPositionBuilder.scmPosition().build()
    )

    def "should return highest version of all when not on head"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("b", "b", "master"),
                [new TagsOnCommit('a', [fullPrefix() + '1.0.0']), new TagsOnCommit('b', [fullPrefix() +'2.0.0'])]),
            false,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '2.0.0'
    }

    def "should return highest version of all when not on head and versions on single commit"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("a", "a", "master"),
                [new TagsOnCommit('a', [fullPrefix() +'1.0.0', fullPrefix() +'2.0.0'])]),
            false,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '2.0.0'
    }


    def "should return highest version of all when alpha is on head"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("b", "b", "master"),
                [new TagsOnCommit('a', [fullPrefix() +'1.0.0']), new TagsOnCommit('b', [fullPrefix() +'2.0.0-alpha'])]),
            false,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '2.0.0'
    }

    def "should prefer normal version to alpha version when on head and versions on single commit"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("a", "a", "master"),
                [new TagsOnCommit('a', [fullPrefix() + '1.0.0', fullPrefix() +'2.0.0-alpha'])]),
            false,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '1.0.0'
    }

    def "should prefer alpha version to normal version when on head, versions on single commit and snapshot is forced"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("a", "a", "master"),
                [new TagsOnCommit('a', [fullPrefix() +'1.0.0', fullPrefix() + '2.0.0-alpha'])]),
            false,
            true,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '2.0.0'
    }

    def "should ignore any nextVersion commits when asked"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("a", "a", "master"),
                [new TagsOnCommit('a', [fullPrefix() +'1.0.0']), new TagsOnCommit('a', [fullPrefix() +'2.0.0-alpha'])]),
            true,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '1.0.0'
    }

    def "should ignore any nextVersion commits when asked and versions on single commit"() {
        when:
        def versionData = sorter.pickTaggedCommit(
            TaggedCommits.fromListOfCommits(new ScmPosition("a", "a", "master"),
                [new TagsOnCommit('a', [fullPrefix() +'1.0.0', fullPrefix() +'2.0.0-alpha'])]),
            true,
            false,
            ~/.*-alpha$/,
            factory
        )

        then:
        versionData.version.toString() == '1.0.0'
    }
}
