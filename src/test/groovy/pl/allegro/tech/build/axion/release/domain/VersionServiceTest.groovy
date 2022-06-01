package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionServiceTest extends Specification {

    VersionResolver resolver = Stub(VersionResolver)

    VersionService service

    VersionConfig versionConfig

    TagProperties tagProperties = tagProperties().build()

    NextVersionProperties nextVersionProperties = nextVersionProperties().build()

    def setup() {
        Project project = Fixtures.project()
        versionConfig = Fixtures.versionConfig(project)

        service = new VersionService(resolver)
    }

    def "should return stable version when on tag"() {
        given:
        VersionProperties properties = versionProperties().build()
        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf('1.0.0'),
            false,
            Version.valueOf('1.0.0'),
            new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(properties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.0'
        !version.snapshot
    }

    def "should return snapshot version with increased patch when forcing snapshot"() {
        given:
        VersionProperties properties = versionProperties().forceSnapshot().build()
        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf('1.0.1'),
            true,
            Version.valueOf('1.0.1'),
            new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(properties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return snapshot version with increased patch when not on tag"() {
        given:
        VersionProperties properties = versionProperties().build()
        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.1"),
            new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(properties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return snapshot version with increased patch when on tag but there are uncommitted changes"() {
        given:
        VersionProperties properties = versionProperties().dontIgnoreUncommittedChanges().build()
        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.1"),
            new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(properties, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return version information"() {
        given:
        VersionProperties properties = versionProperties().withVersionCreator({ v, t -> v }).build()
        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.0"),
            new ScmPosition('', '', 'master')
        )

        when:
        VersionService.DecoratedVersion version = service.currentDecoratedVersion(properties, tagProperties, nextVersionProperties)

        then:
        version.undecoratedVersion == '1.0.1'
        version.decoratedVersion == '1.0.1-SNAPSHOT'
        version.previousVersion == '1.0.0'
    }

    def "should sanitize version if flag is set to true"() {
        given:
        VersionProperties properties = versionProperties().withVersionCreator({ v, t -> return v + '-feature/hello' }).build()

        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.1"),
            new ScmPosition('', '', 'master')
        )

        when:
        String version = service.currentDecoratedVersion(properties, tagProperties, nextVersionProperties).decoratedVersion

        then:
        version == '1.0.1-feature-hello-SNAPSHOT'
    }

    def "should not sanitize version if flag is set to false"() {
        given:
        VersionProperties properties = versionProperties()
            .dontSanitizeVersion()
            .withVersionCreator({ v, t -> return v + '-feature/hello' })
            .build()

        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.1"),
            new ScmPosition('', '', 'master')
        )

        when:
        String version = service.currentDecoratedVersion(properties, tagProperties, nextVersionProperties).decoratedVersion

        then:
        version == '1.0.1-feature/hello-SNAPSHOT'
    }

    def "should allow for customizing snapshot"() {
        given:
        VersionProperties properties = versionProperties()
            .withSnapshotCreator({ v, t -> return ".dirty" } )
            .build()

        resolver.resolveVersion(properties, tagProperties, nextVersionProperties) >> new VersionContext(
            Version.valueOf("1.0.1"),
            true,
            Version.valueOf("1.0.1"),
            new ScmPosition('', '', 'master')
        )

        when:
        String version = service.currentDecoratedVersion(properties, tagProperties, nextVersionProperties).decoratedVersion

        then:
        version == '1.0.1.dirty'
    }
}
