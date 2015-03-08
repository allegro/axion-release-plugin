package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LocalOnlyResolverTest extends Specification {

    private final Project project = ProjectBuilder.builder().build()

    private final VersionConfig config = new VersionConfig(project)

    def "should resolve to localOnly when project release.localOnly property present"() {
        given:
        project.extensions.extraProperties.set('release.localOnly', '')
        LocalOnlyResolver resolver = new LocalOnlyResolver(config, project)

        expect:
        resolver.localOnly(true)
    }

    def "should resolve to localOnly when config has localOnly flag set"() {
        given:
        config.localOnly = true
        LocalOnlyResolver resolver = new LocalOnlyResolver(config, project)

        expect:
        resolver.localOnly(true)
    }

    def "should decide if local only based on remote presence if no flags present"() {
        given:
        LocalOnlyResolver resolver = new LocalOnlyResolver(config, project)
        
        expect:
        resolver.localOnly(false)
        !resolver.localOnly(true)
    }
}
