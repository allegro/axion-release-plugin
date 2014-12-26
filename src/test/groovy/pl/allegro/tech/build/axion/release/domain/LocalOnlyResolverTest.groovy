package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class LocalOnlyResolverTest {

    private final Project project = ProjectBuilder.builder().build()

    private final VersionConfig config = new VersionConfig(project)

    private final LocalOnlyResolver resolver = new LocalOnlyResolver(config, project)

    def "should resolve to localOnly when project release.localOnly property present"() {
        given:
        project.setProperty('release.localOnly')

        expect:
        resolver.localOnly(true)
    }

    def "should resolve to localOnly when config has localOnly flag set"() {
        given:
        config.localOnly = true

        expect:
        resolver.localOnly(true)
    }

    def "should decide if local only based on remote presence if no flags present"() {
        expect:
        resolver.localOnly(false)
        !resolver.localOnly(true)
    }
}
