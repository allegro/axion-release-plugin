package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import spock.lang.Specification

class LocalOnlyResolverFactoryTest extends Specification {

    private final Project project = ProjectBuilder.builder().build()

    private final VersionConfig config = new VersionConfig(project)


    def "should resolve to localOnly when project release.localOnly property present"() {
        given:
        project.extensions.extraProperties.set('release.localOnly', '')
        LocalOnlyResolver resolver = LocalOnlyResolverFactory.create(project, config)

        expect:
        resolver.localOnly(false)
    }

    def "should resolve to localOnly when config has localOnly flag set"() {
        given:
        config.localOnly = true
        LocalOnlyResolver resolver = LocalOnlyResolverFactory.create(project, config)

        expect:
        resolver.localOnly(false)
    }

}
