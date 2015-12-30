package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties

class VersionServiceTest extends Specification {

    VersionResolver resolver = Stub(VersionResolver)

    VersionService service

    TagProperties tagProperties = tagProperties().build()

    NextVersionProperties nextVersionProperties = nextVersionProperties().build()

    def setup() {
        service = new VersionService(resolver)
    }

    def "should return stable version when on tag"() {
        given:
        VersionProperties versionProperties = new VersionProperties([:])
        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf('1.0.0'),
                Version.valueOf('1.0.0'),
                new ScmPosition('master', 'release-1.0.0', true)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.0'
        !version.snapshotVersion
    }

    def "should return snapshot version with increased patch when forcing snapshot"() {
        given:
        VersionProperties versionProperties = new VersionProperties(forceSnapshot: true)
        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf('1.0.1'),
                Version.valueOf('1.0.1'),
                new ScmPosition('master', 'release-1.0.0', true)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should return snapshot version with increased patch when not on tag"() {
        given:
        VersionProperties versionProperties = new VersionProperties([:])
        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should return snapshot version with increased patch when on tag but there are uncommitted changes"() {
        given:
        VersionProperties versionProperties = new VersionProperties(ignoreUncommittedChanges: false)
        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should sanitize version if flag is set to true"() {
        given:
        VersionProperties versionProperties = new VersionProperties(
                sanitizeVersion: true,
                versionCreator: {v, t -> return v + '-feature/hello'}
        )

        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version == '1.0.1-feature-hello-SNAPSHOT'
    }

    def "should not sanitize version if flag is set to false"() {
        given:
        VersionProperties versionProperties = new VersionProperties(
                sanitizeVersion: false,
                versionCreator: {v, t -> return v + '-feature/hello'}
        )
        resolver.resolveVersion(versionProperties, tagProperties, nextVersionProperties) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionProperties, tagProperties, nextVersionProperties)

        then:
        version == '1.0.1-feature/hello-SNAPSHOT'
    }
}
