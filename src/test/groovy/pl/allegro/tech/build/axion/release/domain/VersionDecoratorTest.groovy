package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class VersionDecoratorTest extends Specification {

    VersionDecorator decorator = new VersionDecorator()

    VersionConfig versionConfig

    def setup() {
        versionConfig = new VersionConfig(ProjectBuilder.builder().build())
    }

    def "should use default creator when working with forced version"() {
        given:
        VersionWithPosition rawVersion = VersionWithPosition.withoutPosition(Version.valueOf('0.1.0'))
        versionConfig.with {
            branchVersionCreators = [
                    'someBranch': { v, p -> 'someBranch' }
            ]
        }

        when:
        String version = decorator.createVersion(versionConfig, rawVersion)

        then:
        version == '0.1.0'
    }

    def "should use default creator when no match found"() {
        given:
        VersionWithPosition rawVersion = new VersionWithPosition(
                Version.valueOf('0.1.0'),
                new ScmPosition('master', 'release-0.1.0', false)
        )
        versionConfig.with {
            branchVersionCreators = [
                    'someBranch': { v, p -> 'someBranch' }
            ]
        }

        when:
        String version = decorator.createVersion(versionConfig, rawVersion)

        then:
        version == '0.1.0'
    }

    def "should use branch creator when pattern matches branch"() {
        given:
        VersionWithPosition rawVersion = new VersionWithPosition(
                Version.valueOf('0.1.0'),
                new ScmPosition('someBranch', 'release-0.1.0', false)
        )
        versionConfig.with {
            branchVersionCreators = [
                    'some.*': { v, p -> 'someCustomVersion' }
            ]
        }

        when:
        String version = decorator.createVersion(versionConfig, rawVersion)

        then:
        version == 'someCustomVersion'
    }
}
