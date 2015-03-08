package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class ReleaserTest extends RepositoryBasedTest {

    Releaser releaser
    
    def setup() {
        ScmService scmService = context.scmService()
        
        releaser = new Releaser(scmService, new ReleaseHooksRunner(project.logger, scmService, config.hooks),
                context.localOnlyResolver(),
                project.logger)
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
}
