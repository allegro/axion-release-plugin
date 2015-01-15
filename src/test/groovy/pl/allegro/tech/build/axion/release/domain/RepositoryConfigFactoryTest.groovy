package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class RepositoryConfigFactoryTest extends Specification {

    def "should set repository directory to rootProject dir by default"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        RepositoryConfig config = RepositoryConfigFactory.create(project)

        then:
        config.directory == project.rootDir
    }

    def "should not initialize authorization options when no flags on project"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        RepositoryConfig config = RepositoryConfigFactory.create(project)

        then:
        !config.customKeyPassword
        !config.customKey
    }

    def "should set authorization options when custom key and password provided"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.extensions.extraProperties.set('release.customKey', 'key')
        project.extensions.extraProperties.set('release.customKeyPassword', 'password')

        when:
        RepositoryConfig config = RepositoryConfigFactory.create(project)

        then:
        config.customKey == 'key'
        config.customKeyPassword == 'password'
    }

    def "should read key from file when 'release.customKeyFile' property used"() {
        given:
        Project project = ProjectBuilder.builder().build()

        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        project.extensions.extraProperties.set('release.customKeyFile', keyFile.canonicalPath)
        project.extensions.extraProperties.set('release.customKeyPassword', 'password')

        when:
        RepositoryConfig config = RepositoryConfigFactory.create(project)

        then:
        config.customKey == 'keyFile'
        config.customKeyPassword == 'password'
    }

    def "should prefer explicit custom key before key read from file when both 'release.customKey*' properties used"() {
        given:
        Project project = ProjectBuilder.builder().build()

        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        project.extensions.extraProperties.set('release.customKey', 'key')
        project.extensions.extraProperties.set('release.customKeyFile', keyFile.canonicalPath)
        project.extensions.extraProperties.set('release.customKeyPassword', 'password')

        when:
        RepositoryConfig config = RepositoryConfigFactory.create(project)

        then:
        config.customKey == 'key'
    }

}
