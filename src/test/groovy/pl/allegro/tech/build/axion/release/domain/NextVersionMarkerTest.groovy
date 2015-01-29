package pl.allegro.tech.build.axion.release.domain

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import spock.lang.Specification

class NextVersionMarkerTest extends Specification {
    
    VersionConfig config
    
    VersionService versionService

    ScmRepository repository
    
    NextVersionMarker nextVersionMarker
    
    def setup() {
        Project project = ProjectBuilder.builder().build()
        config = project.extensions.create('scmVersion', VersionConfig, project)

        Grgit.init(dir: project.rootDir)

        Context context = Context.instance(project)
        ScmService scmService = context.scmService()
        scmService.commit('initial commit')
        repository = context.repository()
        versionService = context.versionService()

        nextVersionMarker = new NextVersionMarker(scmService, context.localOnlyResolver(), project.logger)
    }
    
    def "should create next version tag with given version"() {
        when:
        nextVersionMarker.markNextVersion(config, '2.0.0')
        
        then:
        versionService.currentVersion(config, VersionReadOptions.defaultOptions()).version.toString() == '2.0.0-SNAPSHOT'
        repository.currentPosition(~/.*/).latestTag == 'release-2.0.0-alpha'
    }
}
