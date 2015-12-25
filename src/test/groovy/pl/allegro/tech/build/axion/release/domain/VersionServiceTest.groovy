package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class VersionServiceTest extends Specification {

    VersionResolver resolver = Stub(VersionResolver)

    VersionService service

    VersionConfig versionConfig

    TagProperties tagRules = new TagProperties([:])

    NextVersionProperties nextVersionRules = new NextVersionProperties([:])

    def setup() {
        Project project = ProjectBuilder.builder().build()
        versionConfig = project.extensions.create('versionConfig', VersionConfig, project)

        service = new VersionService(resolver)
    }

    def "should return stable version when on tag"() {
        given:
        VersionProperties versionRules = new VersionProperties([:])
        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf('1.0.0'),
                Version.valueOf('1.0.0'),
                new ScmPosition('master', 'release-1.0.0', true)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.version.toString() == '1.0.0'
        !version.snapshotVersion
    }

    def "should return snapshot version with increased patch when forcing snapshot"() {
        given:
        VersionProperties versionRules = new VersionProperties(forceSnapshot: true)
        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf('1.0.1'),
                Version.valueOf('1.0.1'),
                new ScmPosition('master', 'release-1.0.0', true)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should return snapshot version with increased patch when not on tag"() {
        given:
        VersionProperties versionRules = new VersionProperties([:])
        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should return snapshot version with increased patch when on tag but there are uncommitted changes"() {
        given:
        VersionProperties versionRules = new VersionProperties(ignoreUncommittedChanges: false)
        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.version.toString() == '1.0.1'
        version.snapshotVersion
    }

    def "should sanitize version if flag is set to true"() {
        given:
        VersionProperties versionRules = new VersionProperties(
                sanitizeVersion: true,
                versionCreator: {v, t -> return v + '-feature/hello'}
        )

        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionRules, tagRules, nextVersionRules)

        then:
        version == '1.0.1-feature-hello-SNAPSHOT'
    }

    def "should not sanitize version if flag is set to false"() {
        given:
        VersionProperties versionRules = new VersionProperties(
                sanitizeVersion: false,
                versionCreator: {v, t -> return v + '-feature/hello'}
        )
        resolver.resolveVersion(versionRules, tagRules, nextVersionRules) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionRules, tagRules, nextVersionRules)

        then:
        version == '1.0.1-feature/hello-SNAPSHOT'
    }
}
