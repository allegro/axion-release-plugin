package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import spock.lang.Specification

class ScmPushOptionsTest extends Specification {
    
    private Project project = ProjectBuilder.builder().build()
    
    def "should return remote name and tagsOnly option from config when no flags on project"() {
        given:
        RepositoryConfig config = new RepositoryConfig(remote: 'someRemote', pushTagsOnly: true)
        
        when:
        ScmPushOptions pushOptions = ScmPushOptions.fromProject(project, config)
        
        then:
        pushOptions.remote == 'someRemote'
        pushOptions.tagsOnly
    }
    
    def "should return true for tagsOnly when enabled via project flag"() {
        given:
        RepositoryConfig config = new RepositoryConfig()
        project.extensions.extraProperties.set('release.pushTagsOnly', true)

        when:
        ScmPushOptions pushOptions = ScmPushOptions.fromProject(project, config)
        
        then:
        pushOptions.tagsOnly
    }
    
}
