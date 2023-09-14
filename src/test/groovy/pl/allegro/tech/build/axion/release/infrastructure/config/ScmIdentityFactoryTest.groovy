package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import spock.lang.Specification

class ScmIdentityFactoryTest extends Specification {
    private final Project project = Fixtures.project()

    def "should return default identity when no key set"() {
        given:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        when:
        ScmIdentity identity = ScmIdentityFactory.create(config, false)

        then:
        identity.useDefault
        !identity.disableAgentSupport
    }

    def "should return no-ssh-agent identity when support for ssh agents disabled"() {
        given:
        RepositoryConfig config = Fixtures.repositoryConfig(project)

        when:
        ScmIdentity identity = ScmIdentityFactory.create(config, true)

        then:
        identity.useDefault
        identity.disableAgentSupport
    }

    def "should return custom identity when key set"() {
        given:
        RepositoryConfig config = Fixtures.repositoryConfig(project)
        config.customKey.set("key")
        config.customKeyPassword.set("password")

        when:
        ScmIdentity identity = ScmIdentityFactory.create(config, false)

        then:
        !identity.useDefault
        identity.privateKey == 'key'
        identity.passPhrase == 'password'
    }

    def "should read key contents from file when file passed as key"() {
        given:
        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        RepositoryConfig config = Fixtures.repositoryConfig(project)
        config.customKeyFile.set(keyFile)
        config.customKeyPassword.set("password")

        when:
        ScmIdentity identity = ScmIdentityFactory.create(config, false)

        then:
        !identity.useDefault
        identity.privateKey == 'keyFile'
        identity.passPhrase == 'password'
    }
}
