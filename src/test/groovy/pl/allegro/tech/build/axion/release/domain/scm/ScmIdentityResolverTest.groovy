package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ScmIdentityResolverTest extends Specification {

    ScmIdentityResolver resolver = new ScmIdentityResolver()

    def "should return default identity when no flags provided"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        ScmIdentity identity = resolver.resolve(project)

        then:
        identity.useDefault == true
    }

    def "should return identity with key and password provided in flags when 'release.customKey' property used"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.extensions.extraProperties.set('release.customKey', 'key')
        project.extensions.extraProperties.set('release.customKeyPassword', 'password')

        when:
        ScmIdentity identity = resolver.resolve(project)

        then:
        identity.useDefault == false
        identity.privateKey == 'key'
        identity.passPhrase == 'password'
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
        ScmIdentity identity = resolver.resolve(project)

        then:
        identity.useDefault == false
        identity.privateKey == 'keyFile'
        identity.passPhrase == 'password'
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
        ScmIdentity identity = resolver.resolve(project)

        then:
        identity.privateKey == 'key'
    }
}
