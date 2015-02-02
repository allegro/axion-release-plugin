package pl.allegro.tech.build.axion.release.domain

import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import spock.lang.Specification

class VersionResolverTest extends Specification {

    ScmRepository repository = Stub(ScmRepository)

    VersionConfig versionConfig

    VersionResolver resolver

    def setup() {
        versionConfig = new VersionConfig(ProjectBuilder.builder().build())
        resolver = new VersionResolver(repository)
    }

    def "should return current version read from tag"() {
        given:
        // ~/^release.*(|alpha)$/
        repository.currentPosition(_) >> new ScmPosition('master', 'release-1.0.0', true)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '1.0.0'
    }

    def "should return current version with patch version increased when not on tag"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', 'release-1.0.0', false)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '1.0.1'
    }

    def "should return forced version when forcing is on"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', 'release-1.0.0', true)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.withForcedVersion('2.0.0'))

        then:
        version.version.toString() == '2.0.0'
    }

    def "should return custom initial version when no tag exists"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', null, false)

        versionConfig.tag.initialVersion = { r, p -> '0.0.1' }

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '0.0.1'
    }

    def "should return default initial version when no tag exists and initial version not explicitly defined"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', null, false)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '0.1.0'
    }

    def "should deserialize nextVersion before deserializing version when on nextVersion tag"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', 'release-2.0.0-alpha', true)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '2.0.0'
    }
    
    def "should not increment patch version when being on position after next version tag"() {
        given:
        repository.currentPosition(_) >> new ScmPosition('master', 'release-2.0.0-alpha', false)

        when:
        VersionWithPosition version = resolver.resolveVersion(versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.version.toString() == '2.0.0'
    }
}
