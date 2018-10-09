package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import spock.lang.Ignore

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionResolverTest extends RepositoryBasedTest {

    VersionResolver resolver

    TagProperties tagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = nextVersionProperties().build()

    VersionProperties defaultVersionRules = versionProperties().build()

    def setup() {
        resolver = new VersionResolver(repository)
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
        repository.tag('release-1.1.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple tags on last commit"() {
        given:
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0')
        repository.tag('release-1.2.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple release and non-release tags on last commit"() {
        given:
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0')
        repository.tag('release-1.1.5-alpha')
        repository.tag('release-1.2.0')
        repository.tag('release-1.4.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should prefer normal version to nextVersion when both on same commit"() {
        given:
        repository.tag('release-1.1.0-alpha')
        repository.tag('release-1.1.0')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should prefer normal version to newer nextVersion when both on same commit"() {
        given:
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.0.0'
        version.version.toString() == '1.0.0'
        !version.snapshot
    }
    
    // #263: TBD
    // @Ignore("Fix of this particular bad behaviour looks more complex.")
    def "should prefer snapshot of nextVersion when both on current commit and forceSnapshot is enabled"() {
        given:
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0-alpha')
        VersionProperties versionRules = versionProperties().forceSnapshot().build()
        
        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        
        then:
        version.previousVersion.toString() == '1.0.0'
        // #263: also the nextVersion should be taken account when forceSnapshot is specified
        version.version.toString() == '1.1.0'
        version.snapshot
    }

    def "should return unmodified previous and incremented current version when not on tag"(VersionProperties versionRules) {
        given:
        repository.tag('release-1.1.0')
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

    def "should return unmodified previous and incremented current version when not on tag (and force snapshot)"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')
        def versionRulesForceSnapshot = versionProperties().forceSnapshot().build()

        when:
        VersionContext version = resolver.resolveVersion(versionRulesForceSnapshot, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot
    }

    def "should return previous version from last release tag and current from next version when on next version tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag('release-2.0.0-alpha')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        version.snapshot
    }

    // #263: BUG in -Prelease.forceSnapshot and nextVersionTag reproduced here
    def "should return previous version from last release tag and current from next version when on next version tag (and force snapshot)"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag('release-2.0.0-alpha')
        def versionRulesForceSnapshot = versionProperties().forceSnapshot().build()

        when:
        VersionContext version = resolver.resolveVersion(versionRulesForceSnapshot, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'

        // The following assertion failed -- if forceSnapshots was specified, than 'nextReleaseVersion'
        // was incremented once again yielding the 2.0.1-SNAPSHOT version, which is NOT CORRECT.
        version.version.toString() == '2.0.0'
        version.snapshot
    }

    def "should return release version when there is also a next version tag when on release tag"() {
        given:
        repository.tag('release-1.1.0-alpha')
        repository.commit(['*'], 'some commit')
        repository.tag('release-1.1.0')

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
        repository.tag('release-1.1.0')
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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.1')

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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.3.0')
      repository.tag('release-1.5.1')

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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.1')
      repository.tag('release-1.3.0')

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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.3.0')

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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
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
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0-alpha')
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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')

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
      repository.tag('release-1.0.0')
      repository.commit(['*'], 'some commit')
      repository.tag('release-1.5.0')
      repository.commit(['*'], 'some merge from another branch...')
      repository.tag('release-1.2.0')
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

}
