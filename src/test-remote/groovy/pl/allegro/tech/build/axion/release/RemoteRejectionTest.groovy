package pl.allegro.tech.build.axion.release

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.SshConnector
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*

class RemoteRejectionTest extends Specification {

    // defined in Docker run script in build.gradle
    private static final int SSH_PORT = 2222

    def "should return error on push failure"() {
        given:
        File repoDir = File.createTempDir('axion-release', 'tmp')

        Git.cloneRepository()
            .setDirectory(repoDir)
            .setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                void configure(Transport transport) {
                    SshTransport sshTransport = (SshTransport) transport
                    sshTransport.setSshSessionFactory(new SshConnector(ScmIdentity.defaultIdentityWithoutAgents()))
                }
            })
            .setURI("ssh://git@localhost:${SSH_PORT}/git-server/repos/rejecting-repo")
            .call()

        GitRepository repository = new GitRepository(ScmPropertiesBuilder.scmProperties(repoDir).build())

        repository.commit(['*'], 'initial commit')
        repository.tag(fullPrefix() + 'custom')
        repository.commit(['*'], 'commit after ' + fullPrefix() + 'custom')

        when:
        ScmPushResult result = repository.push(ScmIdentity.defaultIdentityWithoutAgents(), new ScmPushOptions('origin', false), true)

        then:
        !result.success
        result.remoteMessage.get().contains("I reject this push!")
    }
}
