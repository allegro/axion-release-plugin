package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Shared
import spock.lang.Specification

class VersionFactoryTest extends Specification {

    static Project project = ProjectBuilder.builder().build()
    
    VersionConfig versionConfig = new VersionConfig(project)

    VersionFactory factory = new VersionFactory()

    def "should return current version read from position"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true), 
                versionConfig.nextVersion
        )
        
        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '1.0.0'
    }

    def "patch version increased when not on tag and default incrementer"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '1.0.1'
    }

    def "patch version increased when not on tag and incrementPatchVersion incrementer"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_PATCH_VERSION.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.0.1'
    }

    def "minor version increased when not on tag and incrementMinorVersion incrementer"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_MINOR_VERSION.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.2', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.1.0'
    }

    def "minor version increased when not on tag and incrementMinorIfNotOnRelease incrementer and on master"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.2', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.1.0'
    }

    def "patch version increased when not on tag and incrementMinorIfNotOnRelease incrementer and on release"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('release/1.0', 'release-1.0.2', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.0.3'
    }

    def "patch version increased when not on tag and incrementMinorIfNotOnRelease incrementer"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH.versionIncrementer
        versionConfig.releaseBranchPattern('ga/.+')
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('ga/1.1', 'release-1.1.0', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '1.1.1'
    }

    def "prerelease version increased when not on tag and incrementPrerelease incrementer"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_PRERELEASE.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-rc1', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '2.0.0-rc2'
    }

    def "patch version increased when not on tag and incrementPrerelease incrementer and prerelease version without trailing digits"() {
        given:
        versionConfig.versionIncrementer = PredefinedVersionIncrementer.INCREMENT_PRERELEASE.versionIncrementer
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-pre', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptionsFactory.empty())

        then:
        version.toString() == '2.0.1'
    }

    def "should return forced version when forcing is on"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, new VersionReadOptions('2.0.0'))

        then:
        version.toString() == '2.0.0'
    }

    def "should return custom initial version when no tag exists"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                versionConfig.nextVersion
        )
        versionConfig.tag.initialVersion = { r, p -> '0.0.1' }

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '0.0.1'
    }

    def "should return default initial version when no tag exists and initial version not explicitly defined"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '0.1.0'
    }

    def "should deserialize nextVersion before deserializing version when on nextVersion tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '2.0.0'
    }
    
    def "should not increment patch version when being on position after next version tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                versionConfig.nextVersion
        )

        when:
        Version version = factory.create(context, versionConfig, VersionReadOptions.defaultOptions())

        then:
        version.toString() == '2.0.0'
    }
}
