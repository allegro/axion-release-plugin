package pl.allegro.tech.build.axion.release.infrastructure.di

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository
import spock.lang.Specification

class ScmRepositoryFactoryTest extends Specification {

    ScmRepositoryFactory factory = new ScmRepositoryFactory()

    Project project

    RepositoryConfig config = new RepositoryConfig()

    def setup() {
        project = ProjectBuilder.builder().build()
        Grgit.init(dir: project.rootDir)

        config.directory = project.rootDir
    }

    def "should return git repository by default"() {
        expect:
        factory.create(project, config) instanceof GitRepository
    }

    def "should return dummy repository when underlying repository is not initialized"() {
        given:
        Project gitlessProject = ProjectBuilder.builder().build()
        config.directory = gitlessProject.rootDir

        expect:
        factory.create(gitlessProject, config) instanceof DummyRepository
    }

    def "should throw exception when trying to construct unknown type of repository"() {
        given:
        config.type = 'unknown'

        when:
        factory.create(project, config)

        then:
        thrown(IllegalArgumentException)
    }
}
