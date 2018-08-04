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

class RemoteRejectionTest extends Specification {

    // defined in Docker run script in build.gradle
    private static final int SSH_PORT = 2222

    def "should return error on push failure"() {
        given:
        File repoDir = File.createTempDir('axion-release', 'tmp')
        GitRepository repository = repositoryFromRemote(repoDir, 'rejecting-repo')

        repository.commit(['*'], 'initial commit')
        repository.tag('release-custom')
        repository.commit(['*'], 'commit after release-custom')

        when:
        ScmPushResult result = repository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: false), true)

        then:
        !result.success
        result.remoteMessage.get().contains("I reject this push!")
    }

    def "should return meaningful error when tag already exists on remote"() {
        given:
        File bootstrapRepoDir = File.createTempDir('axion-release', 'tmp')
        GitRepository bootstrapRepository = repositoryFromRemote(bootstrapRepoDir, 'existing-tag-repo')

        when: 'create initial commit in bare remote repo'
        bootstrapRepository.commit(['*'], 'initial commit')

        ScmPushResult bootstrapResult = bootstrapRepository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: false), true)

        then:
        bootstrapResult.success

        when: 'create repo: CloneX of remote repo'
        File repoDir = File.createTempDir('axion-release', 'tmp')
        GitRepository repository = repositoryFromRemote(repoDir, 'existing-tag-repo')

        and: 'create tag in remote repo which is not known by CloneX'
        bootstrapRepository.tag('release-existing')
        bootstrapRepository.commit(['*'], 'commit after release-custom')

        ScmPushResult result = bootstrapRepository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: false), true)

        then:
        result.success

        and: 'create new commit and tag with existing name in CloneX and push it'
        repository.commit(['*'], 'different commit')
        repository.tag('release-existing')
        ScmPushResult existingResult = repository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: false), true)

        then:
        println "SS: ${existingResult.remoteMessage.get()} EOF"
        !existingResult.success
    }

    private GitRepository repositoryFromRemote(File cloneDirectory, String name) {
        Git.cloneRepository()
            .setDirectory(cloneDirectory)
            .setTransportConfigCallback(new TransportConfigCallback() {
            @Override
            void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport
                sshTransport.setSshSessionFactory(new SshConnector(ScmIdentity.defaultIdentity()))
            }
        })
            .setURI("ssh://git@localhost:${SSH_PORT}/git-server/repos/$name")
            .call()

        return new GitRepository(ScmPropertiesBuilder.scmProperties(cloneDirectory).build())
    }
}
