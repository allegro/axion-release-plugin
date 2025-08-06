package pl.allegro.tech.build.axion.release.infrastructure.config

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class VersionPropertiesFactoryTest extends Specification {

    private Project project

    private VersionConfig versionConfig

    def setup() {
        project = Fixtures.project()
        versionConfig = Fixtures.versionConfig(project)
    }

    def "should copy non-project properties from VersionConfig object"() {
        given:
        versionConfig.versionIncrementer({ VersionIncrementerContext c -> new Version.Builder('1.2.3').build() })
        versionConfig.sanitizeVersion.set(false)

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'master')

        then:
        rules.versionIncrementer.apply(null) == new Version.Builder('1.2.3').build()
        rules.sanitizeVersion == versionConfig.sanitizeVersion.get()
    }

    def "should return forceVersion false when project has no 'release.version' property"() {
        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'master')

        then:
        !rules.forceVersion()
    }

    def "should return ignore uncommitted changes flag from version config when no project flag present"() {
        given:
        versionConfig.ignoreUncommittedChanges.set(false)

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'master')

        then:
        !rules.ignoreUncommittedChanges
    }

    def "should pick default version creator if none branch creators match"() {
        given:
        versionConfig.versionCreator.set((VersionProperties.Creator) { v, p -> 'default' })
        versionConfig.branchVersionCreator.putAll([
                'some.*': { v, p -> 'someBranch' }
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'master')

        then:
        rules.versionCreator.apply(null, null) == 'default'
    }

    def "should pick version creator suitable for current branch if defined in per branch creators"() {
        given:
        versionConfig.versionCreator.set((VersionProperties.Creator) { v, p -> 'default' })
        versionConfig.branchVersionCreator.putAll([
                'some.*': { v, p -> 'someBranch' }
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'someBranch')

        then:
        rules.versionCreator.apply(null, null) == 'someBranch'
    }

    def "should use predefined version creator when supplied with String in per branch creators"() {
        given:
        versionConfig.versionCreator((VersionProperties.Creator) { v, p -> 'default' })
        versionConfig.branchVersionCreator.putAll([
                'some.*': 'versionWithBranch'
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'someBranch')

        then:
        rules.versionCreator.apply('1.0.0', scmPosition('someBranch')) == '1.0.0-someBranch'
    }

    def "should pick default version incrementer if none branch incrementers match"() {
        given:
        versionConfig.versionIncrementer({ VersionIncrementerContext c -> c.currentVersion })
        versionConfig.branchVersionIncrementer.putAll([
                'some.*': { c -> c.currentVersion.incrementMajorVersion() }
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'master')

        then:
        rules.versionIncrementer.apply(
                new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(1)
    }

    def "should pick version incrementer suitable for current branch if defined in per branch incrementer"() {
        given:
        versionConfig.versionIncrementer({ c -> c.currentVersion })
        versionConfig.branchVersionIncrementer.putAll([
                'some.*': { VersionIncrementerContext c -> c.currentVersion.incrementMajorVersion() }
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'someBranch')

        then:
        rules.versionIncrementer.apply(
                new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(2)
    }

    def "should use predefined incrementer creator when supplied with String in per branch incrementer"() {
        given:
        versionConfig.versionCreator({ c -> c.currentVersion })
        versionConfig.branchVersionIncrementer.putAll([
                'some.*': 'incrementMajor'
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'someBranch')

        then:
        rules.versionIncrementer.apply(
                new VersionIncrementerContext(Version.forIntegers(1), scmPosition().build())
        ) == Version.forIntegers(2)
    }

    def "should use predefined incrementer creator with config options when supplied with String in per branch incrementer"() {
        given:
        versionConfig.versionCreator({ c -> c.currentVersion })
        versionConfig.branchVersionIncrementer.putAll([
                'some.*': ['incrementMinorIfNotOnRelease', [releaseBranchPattern: 'someOther.*']]
        ])

        when:
        VersionProperties rules = VersionPropertiesFactory.create(versionConfig, 'someBranch')

        then:
        rules.versionIncrementer.apply(
                new VersionIncrementerContext(Version.forIntegers(1), scmPosition('someBranch'))
        ) == Version.forIntegers(1, 1)
    }
}
