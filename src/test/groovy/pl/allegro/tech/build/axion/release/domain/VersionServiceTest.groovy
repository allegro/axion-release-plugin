package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class VersionServiceTest extends Specification {

    VersionResolver resolver = Stub(VersionResolver)

    VersionService service

    VersionConfig versionConfig

    VersionReadOptions readOptions = VersionReadOptionsFactory.empty()

    def setup() {
        versionConfig = new VersionConfig(ProjectBuilder.builder().build())
        service = new VersionService(resolver)
    }

    def "should return resolver version when on tag"() {
        given:
        resolver.resolveVersion(versionConfig, readOptions) >> new VersionWithPosition(
                Version.valueOf("1.0.0"),
                new ScmPosition('master', 'release-1.0.0', true)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionConfig, readOptions)

        then:
        version.version.toString() == '1.0.0'
    }

    def "should return snapshot version with increased patch when not on tag"() {
        given:
        resolver.resolveVersion(versionConfig, readOptions) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        VersionWithPosition version = service.currentVersion(versionConfig, readOptions)

        then:
        version.version.toString() == '1.0.1-SNAPSHOT'
    }

    def "should sanitize version if flag is set to true"() {
        given:
        versionConfig.versionCreator = {v, t -> return v + '-feature/hello'}

        resolver.resolveVersion(versionConfig, readOptions) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionConfig, readOptions)

        then:
        version == '1.0.1-feature-hello-SNAPSHOT'
    }

    def "should not sanitize version if flag is set to false"() {
        given:
        versionConfig.sanitizeVersion = false
        versionConfig.versionCreator = {v, t -> return v + '-feature/hello'}

        resolver.resolveVersion(versionConfig, readOptions) >> new VersionWithPosition(
                Version.valueOf("1.0.1"),
                new ScmPosition('master', 'release-1.0.0', false)
        )

        when:
        String version = service.currentDecoratedVersion(versionConfig, readOptions)

        then:
        version == '1.0.1-feature/hello-SNAPSHOT'
    }
}
