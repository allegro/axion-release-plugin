package pl.allegro.tech.build.axion.release.infrastructure.config

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class VersionPropertiesFactoryTest extends Specification {

    private Project project

    private VersionConfig versionConfig

    def setup() {
        project = builder().build()
        versionConfig = new VersionConfig(project)
    }

    def "should copy non-project properties from VersionConfig object"() {
        given:
        versionConfig.versionIncrementer = { new Version.Builder('1.2.3').build() }
        versionConfig.sanitizeVersion = false

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.versionIncrementer() == new Version.Builder('1.2.3').build()
        rules.sanitizeVersion == versionConfig.sanitizeVersion
    }

    def "should return forceVersion false when project has no 'release.version' property"() {
        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        !rules.forceVersion()
    }

    def "should return forceVersion false when project has 'release.version' property with empty value"() {
        given:
        project.extensions.extraProperties.set('release.version', '')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        !rules.forceVersion()
    }

    def "should return forceVersion true when project has 'release.version' property with non-empty value"() {
        given:
        project.extensions.extraProperties.set('release.version', 'version')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.forceVersion()
        rules.forcedVersion == 'version'
    }

    def "should return trimmed forcedVersion when project has 'release.Version' property with leading or trailing spaces"() {
        given:
        project.extensions.extraProperties.set('release.version', ' version ')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.forceVersion()
        rules.forcedVersion == 'version'
    }

    def "should return forceVersion true when project has deprecated 'release.forceVersion' property with non-empty value"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', 'version')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.forceVersion()
        rules.forcedVersion == 'version'
    }

    def "should return ignore uncommitted changes flag from version config when no project flag present"() {
        given:
        versionConfig.ignoreUncommittedChanges = false

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        !rules.ignoreUncommittedChanges
    }

    def "should return ignore uncommitted changes as true when project flag present"() {
        given:
        versionConfig.ignoreUncommittedChanges = false
        project.extensions.extraProperties.set('release.ignoreUncommittedChanges', true)

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.ignoreUncommittedChanges
    }

    def "should pick default version creator if none branch creators match"() {
        given:
        versionConfig.versionCreator = { v, p -> 'default' }
        versionConfig.branchVersionCreator = [
            'some.*': { v, p -> 'someBranch' }
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.versionCreator(null, null) == 'default'
    }

    def "should pick version creator suitable for current branch if defined in per branch creators"() {
        given:
        versionConfig.versionCreator = { v, p -> 'default' }
        versionConfig.branchVersionCreator = [
            'some.*': { v, p -> 'someBranch' }
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionCreator(null, null) == 'someBranch'
    }

    def "should use predefined version creator when supplied with String in per branch creators"() {
        given:
        versionConfig.versionCreator = { v, p -> 'default' }
        versionConfig.branchVersionCreator = [
            'some.*': 'versionWithBranch'
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionCreator('1.0.0', scmPosition('someBranch')) == '1.0.0-someBranch'
    }


    def "should use version creator passed as command line option if present"() {
        given:
        versionConfig.versionCreator = { v, p -> 'default' }
        versionConfig.branchVersionCreator = [
            'some.*': 'versionWithBranch'
        ]
        project.extensions.extraProperties.set('release.versionCreator', 'simple')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionCreator('1.0.0', scmPosition('someBranch')) == '1.0.0'
    }

    def "should pick default version incrementer if none branch incrementers match"() {
        given:
        versionConfig.versionIncrementer = { c -> c.currentVersion }
        versionConfig.branchVersionIncrementer = [
            'some.*': { c -> c.currentVersion.incrementMajorVersion() }
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', false)

        then:
        rules.versionIncrementer(
            new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(1)
    }

    def "should pick version incrementer suitable for current branch if defined in per branch incrementer"() {
        given:
        versionConfig.versionIncrementer = { c -> c.currentVersion }
        versionConfig.branchVersionIncrementer = [
            'some.*': { c -> c.currentVersion.incrementMajorVersion() }
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionIncrementer(
            new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(2)
    }

    def "should use predefined incrementer creator when supplied with String in per branch incrementer"() {
        given:
        versionConfig.versionCreator = { c -> c.currentVersion }
        versionConfig.branchVersionIncrementer = [
            'some.*': 'incrementMajor'
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionIncrementer(
            new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(2)
    }

    def "should use predefined incrementer creator with config options when supplied with String in per branch incrementer"() {
        given:
        versionConfig.versionCreator = { c -> c.currentVersion }
        versionConfig.branchVersionIncrementer = [
            'some.*': ['incrementMinorIfNotOnRelease', [releaseBranchPattern: 'someOther.*']]
        ]

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionIncrementer(
            new VersionIncrementerContext(Version.forIntegers(1), scmPosition('someBranch'))
        ) == Version.forIntegers(1, 1)
    }

    def "should use incrementer passed as command line option if present"() {
        given:
        versionConfig.versionCreator = { c -> c.currentVersion }
        project.extensions.extraProperties.set('release.versionIncrementer', 'incrementMajor')

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'someBranch', false)

        then:
        rules.versionIncrementer(
            new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(2)

    }

    def "should set shouldForceIncrement if set"() {
        given:
        versionConfig.versionIncrementer = { new Version.Builder('1.2.3').build() }
        versionConfig.sanitizeVersion = false

        when:
        VersionProperties rules = VersionPropertiesFactory.create(project, versionConfig, 'master', true)

        then:
        rules.isShouldForceIncrementVersion()
    }

}
