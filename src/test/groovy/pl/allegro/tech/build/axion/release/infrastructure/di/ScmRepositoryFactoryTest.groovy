package pl.allegro.tech.build.axion.release.infrastructure.di

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository

import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

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
        given:
        ScmProperties properties = scmProperties(project.rootDir).build()

        expect:
        factory.create(properties) instanceof GitRepository
    }

    def "should return dummy repository when underlying repository is not initialized"() {
        given:
        Project gitlessProject = ProjectBuilder.builder().build()
        ScmProperties properties = scmProperties(gitlessProject.rootDir).build()

        expect:
        factory.create(properties) instanceof DummyRepository
    }

    def "should throw exception when trying to construct unknown type of repository"() {
        given:
        ScmProperties properties = scmProperties(project.rootDir).ofType('unknown').build()

        when:
        factory.create(properties)

        then:
        thrown(IllegalArgumentException)
    }
}
