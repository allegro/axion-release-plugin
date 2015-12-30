package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder

class GitProjectBuilder {
    
    private final File directory

    private final Grgit rawRepository
    
    private ScmProperties scmProperties
    
    private ScmIdentity identity
    
    private GitProjectBuilder(File directory) {
        this.directory = directory

        this.rawRepository = Grgit.init(dir: directory)
        this.scmProperties = ScmPropertiesBuilder.scmProperties(directory).build()
        this.identity = ScmIdentity.defaultIdentity()
    }

    private GitProjectBuilder(File directory, File cloneFrom) {
        this.directory = directory

        this.rawRepository =  Grgit.clone(dir: directory, uri: "file://${cloneFrom.canonicalPath}")
        this.scmProperties = ScmPropertiesBuilder.scmProperties(directory).build()
        this.identity = ScmIdentity.defaultIdentity()
    }
    
    static GitProjectBuilder gitProject(File project) {
        return new GitProjectBuilder(project)
    }
    
    static GitProjectBuilder gitProject(File project, File cloneFrom) {
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
