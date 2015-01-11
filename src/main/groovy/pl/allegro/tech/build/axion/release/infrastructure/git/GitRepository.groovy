package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.BranchStatus
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.operation.FetchOp
import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.TagOpt
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.URIish
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmInitializationOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException

class GitRepository implements ScmRepository {

    private static final String GIT_TAG_PREFIX = 'refs/tags/'

    private final Grgit repository

    GitRepository(File repositoryDir, ScmInitializationOptions options) {
        try {
            repository = Grgit.open(repositoryDir)
        }
        catch(RepositoryNotFoundException exception) {
            throw new ScmRepositoryUnavailableException(exception)
        }

        if (options.attachRemote) {
            this.attachRemote(options.remote, options.remoteUrl)
        }
        if (options.fetchTags) {
            this.fetchTags()
        }
    }

    @Override
    void fetchTags(ScmIdentity identity, String remoteName) {
        identity.useDefault ? callFetch(remoteName) : callLowLevelFetch(identity, remoteName);
    }

    private void callFetch(String remoteName) {
        repository.fetch(remote: remoteName, tagMode: FetchOp.TagMode.ALL, refSpecs: [Transport.REFSPEC_TAGS])
    }

    private void callLowLevelFetch(ScmIdentity identity, String remoteName) {
        FetchCommand fetch = repository.repository.jgit.fetch()
        fetch.remote = remoteName
        fetch.tagOpt = TagOpt.FETCH_TAGS
        fetch.refSpecs = [Transport.REFSPEC_TAGS]
        setTransportOptions(identity, fetch)

        fetch.call()
    }

    @Override
    void tag(String tagName) {
        repository.tag.add(name: tagName)
    }

    @Override
    void push(ScmIdentity identity, String remoteName) {
        push(identity, remoteName, false)
    }

    void push(ScmIdentity identity, String remoteName, boolean all) {
        identity.useDefault ? callPush(remoteName, all) : callLowLevelPush(identity, remoteName, all)
    }

    private void callPush(String remoteName, boolean all) {
        repository.push(remote: remoteName, tags: true, all: all)
    }

    private void callLowLevelPush(ScmIdentity identity, String remoteName, boolean all) {
        pushCommand(identity, remoteName, all).call()
        pushCommand(identity, remoteName, all).setPushTags().call()
    }

    private PushCommand pushCommand(ScmIdentity identity, String remoteName, boolean all) {
        PushCommand push = repository.repository.jgit.push()
        push.remote = remoteName

        if(all) {
            push.setPushAll()
        }

        setTransportOptions(identity, push)

        return push
    }

    private void setTransportOptions(ScmIdentity identity, TransportCommand command) {
        command.transportConfigCallback = new TransportConfigCallback() {
            @Override
            void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport
                sshTransport.setSshSessionFactory(new SshConnector(identity))
            }
        }
    }

    @Override
    void attachRemote(String remoteName, String remoteUrl) {
        Config config = repository.repository.jgit.repository.config

        RemoteConfig remote = new RemoteConfig(config, remoteName)
        // clear other push specs
        List<URIish> pushUris = new ArrayList<>(remote.pushURIs)
        for (URIish uri : pushUris) {
            remote.removePushURI(uri)
        }

        remote.addPushURI(new URIish(remoteUrl))
        remote.update(config)

        config.save()
    }

    @Override
    void commit(String message) {
        repository.add(patterns: ['*'])
        repository.commit(message: message)
    }

    @Override
    ScmPosition currentPosition(String tagPrefix) {
        if(!hasCommits()) {
            return ScmPosition.defaultPosition()
        }

        Map tags = repository.tag.list()
                .grep({ it.fullName.substring(GIT_TAG_PREFIX.length()).startsWith(tagPrefix) })
                .inject([:], { map, entry -> map[entry.commit.id] = entry.fullName.substring(GIT_TAG_PREFIX.length()); return map; })

        ObjectId headId = repository.repository.jgit.repository.resolve(Constants.HEAD)
        String branch = repository.branch.current.name

        RevWalk walk = new RevWalk(repository.repository.jgit.repository)
        walk.sort(RevSort.TOPO)
        RevCommit head = walk.parseCommit(headId)

        String tagName = null

        walk.markStart(head)
        RevCommit commit
        for (commit = walk.next(); commit != null; commit = walk.next()) {
            tagName = tags[commit.id.name()]
            if (tagName != null) {
                break
            }
        }
        walk.dispose()

        boolean onTag = (commit == null) ? null : commit.id.name() == headId.name()
        return new ScmPosition(branch, tagName, onTag)
    }

    private boolean hasCommits() {
        LogCommand log = repository.repository.jgit.log()
        log.maxCount = 1

        try {
            log.call()
            return true
        }
        catch(NoHeadException exception) {
            return false
        }
    }

    @Override
    boolean remoteAttached(String remoteName) {
        Config config = repository.repository.jgit.repository.config

        return config.getSubsections('remote').any { it == remoteName }
    }

    @Override
    boolean checkUncommitedChanges() {
        return !repository.status().isClean()
    }

    @Override
    boolean checkAheadOfRemote() {
        BranchStatus status = repository.branch.status(branch: repository.branch.current.fullName)
        return status.aheadCount != 0 || status.behindCount != 0
    }

    void checkoutBranch(String branchName) {
        repository.checkout(branch: branchName, createBranch: true)
    }

    Status listChanges() {
        return repository.status()
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        return repository.log(maxCommits: messageCount)*.fullMessage
    }
}
