package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class LocalOnlyResolverTest {

    private final VersionConfig config = new VersionConfig()

    private final Project project = ProjectBuilder.builder().build()

    private final LocalOnlyResolver resolver = new LocalOnlyResolver(config, project)

    def "should resolve to localOnly when project release.localOnly property present"() {
        given:
        project.setProperty('release.localOnly')

        expect:
        resolver.localOnly(true) == true
    }

    def "should resolve to localOnly when config has localOnly flag set"() {
        given:
        config.localOnly = true

        expect:
        resolver.localOnly(true) == true
    }

    def "should decide if local only based on remote presence if no flags present"() {
        expect:
        resolver.localOnly(false) == true
        resolver.localOnly(true) == false
    }
}
