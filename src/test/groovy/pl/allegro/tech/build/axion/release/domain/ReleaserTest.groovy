package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.ReleaseBranchesConfiguration
import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix
import static pl.allegro.tech.build.axion.release.domain.properties.HooksPropertiesBuilder.hooksProperties
import static pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder.properties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class ReleaserTest extends RepositoryBasedTest {

    private Releaser releaser

    def setup() {
        ScmService scmService = context.scmService()
        VersionService versionService = context.versionService()

        releaser = new Releaser(versionService, scmService, new ReleaseHooksRunner(versionService, scmService))
    }

    def "should release new version when not on tag"() {
        given:
        Properties rules = properties()
            .withVersionRules(versionProperties().forceVersion('2.0.0').build())
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '2.0.0'
    }

    def "should not release version when on tag"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')

        when:
        releaser.release(context.rules(), new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '1.0.0'
    }

    def "should release with forced pre-released versions"() {
        given:
        Properties rules = properties()
            .withVersionRules(versionProperties().forceVersion('3.0.0-rc4').build())
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '3.0.0-rc4'
    }

    def "should not release version when on pre-released version tag"() {
        given:
        repository.tag(fullPrefix() + '3.0.0-rc4')

        when:
        releaser.release(context.rules(), new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '3.0.0-rc4'
    }

    def "should increment pre-released version correctly"() {
        given:
        repository.tag(fullPrefix() + '3.0.0-rc4')
        repository.commit(['*'], 'make is snapshot')

        when:
        releaser.release(context.rules(), new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '3.0.1'
    }

    def "should create release commit if configured"() {
        given:
        Properties rules = properties()
            .withVersionRules(versionProperties().forceVersion('3.0.0').build())
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '3.0.0'
        repository.lastLogMessages(1) == ['release version: 3.0.0']
    }

    def "should create release commit when on tag but forced"() {
        given:
        repository.tag(fullPrefix() + '3.1.0')
        Properties rules = properties()
            .withVersionRules(versionProperties().forceVersion('3.2.0').build())
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(false, 'main', ['main'] as Set))

        then:
        currentVersion() == '3.2.0'
        repository.lastLogMessages(1) == ['release version: 3.2.0']
    }

    def "should do release when releaseOnlyOnReleaseBranches option is set and current branch is in releaseBranchNames list"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        Properties rules = properties()
            .withVersionRules(versionProperties().forceVersion('1.0.1').build())
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(true, 'main', ['main'] as Set))

        then:
        currentVersion() == '1.0.1'
    }

    def "should skip release when current branch is not in releaseBranchNames list"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        Properties rules = properties()
            .withHooksRules(hooksProperties().withCommitHook().build())
            .build()

        when:
        releaser.release(rules, new ReleaseBranchesConfiguration(true, 'feature/branch', ['main'] as Set))

        then:
        currentVersion() == '1.0.0'
    }
}
