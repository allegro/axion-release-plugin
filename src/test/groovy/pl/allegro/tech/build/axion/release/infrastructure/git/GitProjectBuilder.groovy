package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder

class GitProjectBuilder {
    
    private final Project project
    
    private final File repositoryDir
    
    private final Grgit rawRepository
    
    private ScmProperties scmProperties
    
    private ScmIdentity identity
    
    private GitProjectBuilder(Project project) {
        this.project = project
        this.repositoryDir = project.file('./')

        this.rawRepository = Grgit.init(dir: repositoryDir)
        this.scmProperties = ScmPropertiesBuilder.scmProperties(repositoryDir).build()
        this.identity = ScmIdentity.defaultIdentity()
    }

    private GitProjectBuilder(Project project, Project cloneFrom) {
        this.project = project
        this.repositoryDir = project.file('./')

        this.rawRepository =  Grgit.clone(dir: repositoryDir, uri: "file://${cloneFrom.file('./').canonicalPath}")
        this.scmProperties = ScmPropertiesBuilder.scmProperties(repositoryDir).build()
        this.identity = ScmIdentity.defaultIdentity()
    }
    
    static GitProjectBuilder gitProject(Project project) {
        return new GitProjectBuilder(project)
    }
    
    static GitProjectBuilder gitProject(Project project, Project cloneFrom) {
        return new GitProjectBuilder(project, cloneFrom)
    }

    GitProjectBuilder withInitialCommit() {
        return withInitialCommit('InitialCommit')
    }
    
    GitProjectBuilder withInitialCommit(String message) {
        rawRepository.add(patterns: ['*'])
        rawRepository.commit(message: message)
        return this
    }
    
    GitProjectBuilder usingProperties(ScmProperties scmProperties) {
        this.scmProperties = scmProperties
        return this
    }

    GitProjectBuilder usingIdentity(ScmIdentity identity) {
        this.identity = identity
        return this
    }
    
    Map build() {
        Map map = [:]
        map[Grgit] = rawRepository
        map[GitRepository] = new GitRepository(scmProperties)
        
        return map
    }
    
}
