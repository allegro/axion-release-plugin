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
import org.eclipse.jgit.transport.*
import pl.allegro.tech.build.axion.release.domain.scm.*

import java.util.regex.Pattern

class GitRepository implements ScmRepository {

    private static final String GIT_TAG_PREFIX = 'refs/tags/'

    private final TransportConfigFactory transportConfigFactory = new TransportConfigFactory()
    
    private final File repositoryDir
    
    private final Grgit repository

    private final boolean pushTagsOnly

    GitRepository(File repositoryDir, ScmIdentity identity, ScmInitializationOptions options) {
        try {
            this.repositoryDir = repositoryDir
            repository = Grgit.open(dir: repositoryDir)
        }
        catch(RepositoryNotFoundException exception) {
            throw new ScmRepositoryUnavailableException(exception)
        }

        if (options.attachRemote) {
            this.attachRemote(options.remote, options.remoteUrl)
        }
        if (options.fetchTags) {
            this.fetchTags(identity, options.remote)
        }

        this.pushTagsOnly = options.pushTagsOnly
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
        String headId = repository.repository.jgit.repository.resolve(Constants.HEAD).name()
        boolean isOnExistingTag = repository.tag.list().any({it.name == tagName && it.commit.id == headId})
        if (!isOnExistingTag) {
            repository.tag.add(name: tagName)
        }
    }

    @Override
    void push(ScmIdentity identity, String remoteName) {
        push(identity, remoteName, false)
    }

    void push(ScmIdentity identity, String remoteName, boolean all) {
        identity.useDefault ? callPush(remoteName, all) : callLowLevelPush(identity, remoteName, all)
    }

    private void callPush(String remoteName, boolean all) {
        if(pushTagsOnly == false) {
            repository.push(remote: remoteName, all: all)
        }
        repository.push(remote: remoteName, tags: true, all: all)
    }

    private void callLowLevelPush(ScmIdentity identity, String remoteName, boolean all) {
        if(pushTagsOnly == false) {
            pushCommand(identity, remoteName, all).call()
        }
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
        command.transportConfigCallback = transportConfigFactory.create(identity)
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
    void commit(List patterns, String message) {
        if(!patterns.isEmpty()) {
            String canonicalPath = Pattern.quote(repositoryDir.canonicalPath + File.separatorChar)
            repository.add(patterns: patterns.collect { it.replaceFirst(canonicalPath, '') })
        }
        repository.commit(message: message)
    }

    @Override
    ScmPosition currentPosition(Pattern pattern) {
        return currentPosition(pattern, Pattern.compile('$a^'))
    }
    
    @Override
    ScmPosition currentPosition(Pattern pattern, Pattern inversePattern) {
        if(!hasCommits()) {
            return ScmPosition.defaultPosition()
        }

        Map tags = repository.tag.list()
                .grep({ def tag = it.fullName.substring(GIT_TAG_PREFIX.length()); tag ==~ pattern && !(tag ==~ inversePattern) })
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
    boolean checkUncommittedChanges() {
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
