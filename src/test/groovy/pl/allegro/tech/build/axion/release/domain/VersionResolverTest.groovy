package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

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
    
    def "should return unmodified previous and incremented current version when not on tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

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
    
    def "should return previous version from last release and current from forced version when forcing version"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')

        VersionProperties versionRules = versionProperties().forceVersion('2.0.0').build()

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        version.snapshot
    }
}
