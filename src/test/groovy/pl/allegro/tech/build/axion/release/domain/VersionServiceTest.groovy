package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
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

    VersionConfig versionConfig

    TagProperties tagProperties = tagProperties().build()

    NextVersionProperties nextVersionProperties = nextVersionProperties().build()

    def setup() {
        Project project = ProjectBuilder.builder().build()
        versionConfig = project.extensions.create('versionConfig', VersionConfig, project)

        service = new VersionService(resolver)
    }

    def "should return stable version when on tag"() {
        given:
        VersionProperties versionRules = new VersionProperties([:])
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf('1.0.0'),
                false,
                Version.valueOf('1.0.0'),
                new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(versionRules, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.0'
        !version.snapshot
    }

    def "should return snapshot version with increased patch when forcing snapshot"() {
        given:
        VersionProperties versionRules = new VersionProperties(forceSnapshot: true)
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf('1.0.1'),
                true,
                Version.valueOf('1.0.1'),
                new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(versionRules, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return snapshot version with increased patch when not on tag"() {
        given:
        VersionProperties versionRules = new VersionProperties([:])
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf("1.0.1"),
                true,
                Version.valueOf("1.0.1"),
                new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(versionRules, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return snapshot version with increased patch when on tag but there are uncommitted changes"() {
        given:
        VersionProperties versionRules = new VersionProperties(ignoreUncommittedChanges: false)
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf("1.0.1"),
                true,
                Version.valueOf("1.0.1"),
                new ScmPosition('', '', 'master')
        )

        when:
        VersionContext version = service.currentVersion(versionRules, tagProperties, nextVersionProperties)

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return both decorated and undecorated version"() {
        given:
        VersionProperties versionRules = new VersionProperties(versionCreator: { v, t -> v })
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf("1.0.1"),
                true,
                Version.valueOf("1.0.1"),
                new ScmPosition('', '', 'master')
        )

        when:
        VersionService.DecoratedVersion version = service.currentDecoratedVersion(versionRules, tagProperties, nextVersionProperties)

        then:
        version.undecoratedVersion == '1.0.1'
        version.decoratedVersion == '1.0.1-SNAPSHOT'
    }

    def "should sanitize version if flag is set to true"() {
        given:
        VersionProperties versionRules = new VersionProperties(
                sanitizeVersion: true,
                versionCreator: { v, t -> return v + '-feature/hello' }
        )

        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf("1.0.1"),
                true,
                Version.valueOf("1.0.1"),
                new ScmPosition('', '', 'master')
        )

        when:
        String version = service.currentDecoratedVersion(versionRules, tagProperties, nextVersionProperties).decoratedVersion

        then:
        version == '1.0.1-feature-hello-SNAPSHOT'
    }

    def "should not sanitize version if flag is set to false"() {
        given:
        VersionProperties versionRules = new VersionProperties(
                sanitizeVersion: false,
                versionCreator: { v, t -> return v + '-feature/hello' }
        )
        resolver.resolveVersion(versionRules, tagProperties, nextVersionProperties) >> new VersionContext(
                Version.valueOf("1.0.1"),
                true,
                Version.valueOf("1.0.1"),
                new ScmPosition('', '', 'master')
        )

        when:
        String version = service.currentDecoratedVersion(versionRules, tagProperties, nextVersionProperties).decoratedVersion

        then:
        version == '1.0.1-feature/hello-SNAPSHOT'
    }
}
