package pl.allegro.tech.build.axion.release

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import spock.lang.Specification

class RepositoryBasedTest extends Specification {

    Project project
    
    Context context
    
    VersionConfig config
    
    def setup() {
        project = ProjectBuilder.builder().build()
        project.extensions.create('scmVersion', VersionConfig, project)

        Grgit.init(dir: project.rootDir)
        context = Context.ephemeralInstance(project)
        
        config = context.config()
    }
    
}
