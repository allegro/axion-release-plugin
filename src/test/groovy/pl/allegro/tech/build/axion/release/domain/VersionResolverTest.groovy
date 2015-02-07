package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class VersionResolverTest extends RepositoryBasedTest {
    
    VersionResolver resolver
    
    ScmRepository repository
    
    VersionReadOptions options = VersionReadOptions.defaultOptions()
    
    def setup() {
        repository = context.repository()
        repository.commit(['*'], 'initial commit')

        resolver = new VersionResolver(repository, context.versionFactory())
    }
    
    def "should return default previous and current version when no tag in repository"() {
        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)
        
        then:
        version.previousVersion.toString() == '0.1.0'
        version.version.toString() == '0.1.0'
        version.position.tagless()
    }
    
    def "should return same previous and current version when on release tag"() {
        given:
        repository.tag('release-1.1.0')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
    }
    
    def "should return unmodified previous and incremented current version when not on tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
    }
    
    def "should return previous version from last release tag and current from alpha when on alpha tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag('release-2.0.0-alpha')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        !version.position.onTag
    }
}
