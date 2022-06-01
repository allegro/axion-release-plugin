package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*
import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionResolverTest extends RepositoryBasedTest {

    VersionResolver resolver

    TagProperties tagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = nextVersionProperties().build()

    VersionProperties defaultVersionRules = versionProperties().build()

    def setup() {
        resolver = new VersionResolver(repository, "")
    }

    def "should return default previous and current version when no tag in repository"() {
        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '0.1.0'
        version.version.toString() == '0.1.0'
        version.snapshot
    }

    def "should return same previous and current version when on release tag"() {
        given:
        repository.tag(fullPrefix()+'1.1.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple tags on last commit"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() + '1.1.0')
        repository.tag(fullPrefix() + '1.2.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple release and non-release tags on last commit"() {
        given:
        repository.tag(fullPrefix() +'1.0.0')
        repository.tag(fullPrefix() + '1.1.0')
        repository.tag(fullPrefix() +'1.1.5-alpha')
        repository.tag(fullPrefix() +'1.2.0')
        repository.tag(fullPrefix() +'1.4.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should prefer normal version to nextVersion when both on same commit"() {
        given:
        repository.tag(fullPrefix() + '1.1.0-alpha')
        repository.tag(fullPrefix() + '1.1.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should prefer normal version to newer nextVersion when both on same commit"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() + '1.1.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.0.0'
        version.version.toString() == '1.0.0'
        !version.snapshot
    }

    def "should prefer snapshot of nextVersion when both on current commit and forceSnapshot is enabled"() {

        given: "there is releaseTag and nextVersionTag on current commit"
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() + '1.1.0-alpha')
        VersionProperties versionRules = versionProperties().forceSnapshot().build()

        when: "resolving version with property 'release.forceSnapshot'"
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then: "the resolved version should be snapshot towards the next version"
        version.previousVersion.toString() == '1.0.0'
        version.version.toString() == '1.1.0'
        version.snapshot
    }

    def "should return unmodified previous and incremented current version when not on tag"(VersionProperties versionRules) {
        given:
        repository.tag(fullPrefix() +'1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot

        where:
        versionRules << [
            versionProperties().build(),
            versionProperties().forceSnapshot().build()
        ]
    }

    def "should return previous version from last release tag and current from next version when on next version tag"() {
        given:
        repository.tag(fullPrefix() + '1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() +'2.0.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        version.snapshot
    }

    // This test case reproduces issue #263
    def "should return previous version from last release tag and current from next version when on next version tag (and force snapshot)"() {

        given: "there is nextVersionTag on current commit (2.0.0-alpha)"
        repository.tag(fullPrefix() + '1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '2.0.0-alpha')
        def versionRulesForceSnapshot = versionProperties().forceSnapshot().build()

        when: "resolving version with property 'release.forceSnapshot'"
        VersionContext version = resolver.resolveVersion(versionRulesForceSnapshot, tagRules, nextVersionRules)

        then: "the resolved version should be snapshot towards the next version (2.0.0-SNAPSHOT)"
        version.previousVersion.toString() == '1.1.0'

        // The following assertion fails -- if forceSnapshots specified, than 'nextReleaseVersion'
        // is incremented once again yielding the 2.0.1-SNAPSHOT version, which is NOT CORRECT.
        version.version.toString() == '2.0.0'
        version.snapshot
    }


    def "should return release version when there is also a next version tag when on release tag"() {
        given:
        repository.tag(fullPrefix() + '1.1.0-alpha')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.1.0')

        VersionProperties versionRules = versionProperties().useHighestVersion().build()

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should return previous version from last release and current from forced version when forcing version"(VersionProperties versionRules) {
        given:
        repository.tag(fullPrefix() +'1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        version.snapshot

        where:
        versionRules << [
            versionProperties().forceVersion('2.0.0').build(),
            versionProperties().forceVersion('2.0.0').forceSnapshot().build()
        ]
    }

    def "should still return the same versions when the final tag is tagged as the release"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() +'1.5.1')

        VersionProperties versionProps = versionProperties().useHighestVersion().build()

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.5.1'
        version.version.toString() == '1.5.1'
        !version.snapshot
    }

    def "should still return the same versions when the final tag is tagged as the release with multi-tags"() {
        given:
        repository.tag(fullPrefix() +'1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() +'1.3.0')
        repository.tag(fullPrefix() +'1.5.1')

        VersionProperties versionProps = versionProperties().useHighestVersion().build()

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.5.1'
        version.version.toString() == '1.5.1'
        !version.snapshot
    }

    def "should still return the same versions when the final tag is tagged as the release with multi-tags reversed"() {
        given:
        repository.tag(fullPrefix()+'1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() +'1.2.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.1')
        repository.tag(fullPrefix() +'1.3.0')

        VersionProperties versionProps = versionProperties().useHighestVersion().build()

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.5.1'
        version.version.toString() == '1.5.1'
        !version.snapshot
    }

    def "should return the highest version from the tagged versions"(VersionProperties versionProps) {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.3.0')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)
        println "Version Resolved: $version"

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().useHighestVersion().build(),
            versionProperties().useHighestVersion().forceSnapshot().build()
        ]
    }

    def "should return the highest version from the tagged versions when not on release"(VersionProperties versionProps) {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().useHighestVersion().build(),
            versionProperties().useHighestVersion().forceSnapshot().build()
        ]
    }

    def "should return snapshot version of the more recent version when final and snapshot tags on the same commit in the past"(VersionProperties versionProps) {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() + '1.1.0-alpha')
        repository.commit(['*'], 'some commit')
        repository.commit(['*'], 'some merge from another branch...')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.0.0'
        version.version.toString() == '1.1.0'
        version.snapshot

        where:
        versionProps << [
            versionProperties().build(),
            versionProperties().forceSnapshot().build()
        ]
    }

    def "should return the last version as release from the tagged versions no highest version option selected"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')

        VersionProperties versionProps = versionProperties().build()

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should return the last version from the tagged versions no highest version option selected"(VersionProperties versionProps) {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.5.0')
        repository.commit(['*'], 'some merge from another branch...')
        repository.tag(fullPrefix() + '1.2.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().build(),
            versionProperties().forceSnapshot().build()
        ]
    }

    def "should distinguish between prefixes with shared characters"(VersionProperties versionProps, String tagPrefix, String v, boolean isSnapshot) {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag('B' + fullPrefix() + '1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag(fullPrefix() + '1.1.0')
        repository.tag('B' + fullPrefix() + '1.2.0')

        when:
        TagProperties tagProps = tagProperties().withPrefix(tagPrefix).build()
        VersionContext version = resolver.resolveVersion(versionProps, tagProps, nextVersionRules)

        then:
        version.previousVersion.toString() == v
        version.version.toString() == v
        version.snapshot == isSnapshot

        where:

        versionProps                | tagPrefix | v       | isSnapshot
        versionProperties().build() | defaultPrefix()     | '1.1.0' | false
        versionProperties().build() | 'B'+defaultPrefix() | '1.2.0' | false
    }
}
