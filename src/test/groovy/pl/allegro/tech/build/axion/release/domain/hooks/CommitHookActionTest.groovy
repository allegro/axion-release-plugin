package pl.allegro.tech.build.axion.release.domain.hooks

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import spock.lang.Specification

class CommitHookActionTest extends Specification {
    
    ScmRepository repository
    
    ScmService scmService
    
    def setup() {
        Project project = ProjectBuilder.builder().build()
        project.extensions.create('scmVersion', VersionConfig, project)
        
        Grgit.init(dir: project.rootDir)
        Context context = Context.instance(project)
        
        repository = context.repository()
        scmService = context.scmService()
    }
    
    def "should commit files matching patterns with given message"() {
        given:
        CommitHookAction hook = new CommitHookAction({v, p -> "test of version $v"})
        HookContext context = new HookContextBuilder(scmService: scmService, previousVersion: '1.0.0', currentVersion: '2.0.0').build()
        context.addCommitPattern('*')
        
        when:
        hook.act(context)
        
        then:
        repository.lastLogMessages(1) == ['test of version 2.0.0']
    }
}
