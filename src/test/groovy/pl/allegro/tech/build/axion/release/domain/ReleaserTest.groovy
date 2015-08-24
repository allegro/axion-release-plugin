package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class ReleaserTest extends RepositoryBasedTest {

    Releaser releaser
    
    def setup() {
        ScmService scmService = context.scmService()
        
        releaser = new Releaser(scmService, new ReleaseHooksRunner(project.logger, config, scmService, config.hooks), project)
    }

    def "should release new version when not on tag"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', '2.0.0')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '2.0.0'
    }

    def "should not release version when on tag"() {
        given:
        repository.tag('release-1.0.0')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '1.0.0'
    }

    def "should release with forced pre-released versions"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', '3.0.0-rc4')
        config.createReleaseCommit = false

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.0.0-rc4'
    }

    def "should not release version when on pre-released version tag"() {
        given:
        repository.tag('release-3.0.0-rc4')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.0.0-rc4'
    }

    def "should increment pre-released version correctly"() {
        given:
        repository.tag('release-3.0.0-rc4')
        repository.commit(['*'], 'make is snapshot')

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.0.1'
    }

    def "should create release commit if configured"() {
        given:
        project.extensions.extraProperties.set('release.forceVersion', '3.0.0')
        config.createReleaseCommit = true

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.0.0'
        repository.lastLogMessages(1) == ['release version: 3.0.0']
    }

    def "should create release commit when on tag but forced"() {
        given:
        repository.tag('release-3.1.0')
        project.extensions.extraProperties.set('release.forceVersion', '3.2.0')
        config.createReleaseCommit = true

        when:
        releaser.release(config)

        then:
        config.getVersion() == '3.2.0'
        repository.lastLogMessages(1) == ['release version: 3.2.0']
    }
}
