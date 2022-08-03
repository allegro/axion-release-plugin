package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.Fixtures
import spock.lang.Specification

class RepositoryConfigFactoryTest extends Specification {

    def "should set repository directory to rootProject dir by default"() {
        given:
        Project project = Fixtures.project()

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        config.directory.asFile.get() == project.rootDir
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

    def "should set authorization options when custom key and password provided"() {
        given:
        Project project = Fixtures.project(
            ["release.customKey": "key", "release.customKeyPassword": "password"])

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        config.customKey.get() == "key"
        config.customKeyPassword.get() == "password"
    }
/*
    def "should read key from file when 'release.customKeyFile' property used"() {
        given:
        Project project = Fixtures.project(
            [
             'release.customKeyFile': './keyFile',
             'release.customKeyPassword': 'password']
        )

        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        config.customKeyFile.asFile.get() == './keyFile'
        config.customKeyPassword.get() == 'password'
    }

    def "should prefer explicit custom key before key read from file when both 'release.customKey*' properties used"() {
        given:
        Project project = Fixtures.project(
            ['release.customKey': 'key',
             'release.customKeyFile': './keyFile',
             'release.customKeyPassword': 'password']
        )

        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        config.customKey.get() == 'key'
    }*/

    def "should set username and password when provided via 'release.customUsername' and 'release.customPassword'"() {
        given:
        Project project = Fixtures.project(
            ['release.customUsername': 'username',
             'release.customPassword': 'password']
        )

        when:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        then:
        config.customUsername.get() == 'username'
        config.customPassword.get() == 'password'
    }

}
