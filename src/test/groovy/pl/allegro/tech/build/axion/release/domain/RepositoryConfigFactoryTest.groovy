package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.Fixtures
import spock.lang.Specification

class RepositoryConfigFactoryTest extends Specification {

    def "should set repository directory to rootProject dir by default"() {
        given:
        Project project = Fixtures.project()

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        new File(config.directory.get()) == project.rootDir
    }

    def "should not initialize authorization options when no flags on project"() {
        given:
        Project project = Fixtures.project()

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        !config.customKeyPassword.isPresent()
        !config.customKey.isPresent()
        !config.customUsername.isPresent()
        !config.customPassword.isPresent()
    }
}
