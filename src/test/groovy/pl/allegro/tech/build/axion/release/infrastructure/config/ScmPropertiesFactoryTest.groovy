package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import spock.lang.Specification

class ScmPropertiesFactoryTest extends Specification {

    private Project project = ProjectBuilder.builder().build()

    private VersionConfig config = new VersionConfig(project)

    def "should return remote name and tagsOnly option from configuration when no flags on project"() {
        given:
        config.repository.remote = 'someRemote'
        config.repository.pushTagsOnly = true

        when:
        ScmProperties properties = ScmPropertiesFactory.create(project, config)

        then:
        properties.remote == 'someRemote'
        properties.pushTagsOnly
    }

    def "should return true for tagsOnly when enabled via project flag"() {
        given:
        config.repository.pushTagsOnly = false
        project.extensions.extraProperties.set('release.pushTagsOnly', true)

        when:
        ScmProperties properties = ScmPropertiesFactory.create(project, config)

        then:
        properties.pushTagsOnly
    }

}
