package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.BranchStatus
import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.*
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmInitializationOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class GitRepository implements ScmRepository {

    private static final String GIT_TAG_PREFIX = 'refs/tags/'

    private final Grgit repository

    private final ScmIdentity identity

    GitRepository(File repositoryDir, ScmIdentity identity, Logger logger) {
        try {
            repository = Grgit.open(repositoryDir)
        }
        catch(RepositoryNotFoundException exception) {
            logger.warn("No repository found at $repositoryDir.canonicalPath , continuing with defaults.")
            repository = null
        }

        this.identity = identity
    }

    @Override
    void initialize(ScmInitializationOptions options) {
        if (options.attachRemote) {
            this.attachRemote(options.remote, options.remoteUrl)
        }
        if (options.fetchTags) {
            this.fetchTags()
        }
    }

    private void ensureRepositoryExists() {
        if(repository == null) {
            throw new IllegalStateException("Trying to execute command in an uninitialized repository.")
        }
    }

    @Override
    void fetchTags() {
        ensureRepositoryExists()

        FetchCommand fetch = repository.repository.jgit.fetch()
        fetch.tagOpt = TagOpt.FETCH_TAGS
        fetch.refSpecs = [Transport.REFSPEC_TAGS]
        setTransportOptions(fetch)

        fetch.call()
    }

    @Override
    void tag(String tagName) {
        ensureRepositoryExists()

        repository.tag.add(name: tagName)
    }

    @Override
    void push(String remoteName) {
        ensureRepositoryExists()

        pushCommand(remoteName).call()
        pushCommand(remoteName).setPushTags().call()
    }

    private PushCommand pushCommand(String remoteName) {
        PushCommand push = repository.repository.jgit.push()
        push.remote = remoteName
        setTransportOptions(push)

        return push
    }

    private void setTransportOptions(TransportCommand command) {
        command.transportConfigCallback = new TransportConfigCallback() {
            @Override
            void configure(Transport transport) {
                if (!identity.useDefault) {
                    SshTransport sshTransport = (SshTransport) transport
                    sshTransport.setSshSessionFactory(new SshConnector(identity))
                }
            }
        }
    }

    @Override
    void attachRemote(String remoteName, String remoteUrl) {
        ensureRepositoryExists()

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
        ensureRepositoryExists()

        repository.add(patterns: ['*'])
        repository.commit(message: message)
    }

    @Override
    ScmPosition currentPosition(String tagPrefix) {
        if(repository == null || !hasCommits()) {
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
        ensureRepositoryExists()

        return !repository.status().isClean()
    }

    @Override
    boolean checkAheadOfRemote() {
        ensureRepositoryExists()

        BranchStatus status = repository.branch.status(branch: repository.branch.current.fullName)
        return status.aheadCount != 0 || status.behindCount != 0
    }

    void checkoutBranch(String branchName) {
        repository.repository.jgit.checkout().setName(branchName).setCreateBranch(true).call()
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        ensureRepositoryExists()

        return repository.log(maxCommits: messageCount)*.fullMessage
    }
}
