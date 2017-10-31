package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.BranchStatus
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.operation.FetchOp
import org.eclipse.jgit.api.FetchCommand
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.TagOpt
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.URIish
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmException
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit

import java.util.regex.Pattern

class GitRepository implements ScmRepository {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(GitRepository)

    private static final String GIT_TAG_PREFIX = 'refs/tags/'

    private final TransportConfigFactory transportConfigFactory = new TransportConfigFactory()

    private final File repositoryDir

    private final Grgit repository

    private final ScmProperties properties

    GitRepository(ScmProperties properties) {
        try {
            this.repositoryDir = properties.directory
            repository = Grgit.open(dir: repositoryDir)
            this.properties = properties
        }
        catch (RepositoryNotFoundException exception) {
            throw new ScmRepositoryUnavailableException(exception)
        }

        if (properties.attachRemote) {
            this.attachRemote(properties.remote, properties.remoteUrl)
        }
        if (properties.fetchTags) {
            this.fetchTags(properties.identity, properties.remote)
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
        String headId = repository.repository.jgit.repository.resolve(Constants.HEAD).name()
        boolean isOnExistingTag = repository.tag.list().any({ it.name == tagName && it.commit.id == headId })
        if (!isOnExistingTag) {
            repository.tag.add(name: tagName)
        } else {
            logger.debug("The head commit $headId already has the tag $tagName.")
        }
    }

    @Override
    void dropTag(String tagName) {
        callDropTag(tagName)
    }

    private void callDropTag(String tagName) {
        try {
            repository.tag.remove(names: [tagName])
        } catch (GitAPIException e) {
            throw new ScmException(e)
        }
    }

    @Override
    void push(ScmIdentity identity, ScmPushOptions pushOptions) {
        push(identity, pushOptions, false)
    }

    void push(ScmIdentity identity, ScmPushOptions pushOptions, boolean all) {
        PushCommand command = pushCommand(identity, pushOptions.remote, all)

        if (!pushOptions.pushTagsOnly) {
            verifyPushResults(callPush(command))
        }

        verifyPushResults(callPush(command.setPushTags()))
    }

    private Iterable<PushResult> callPush(PushCommand pushCommand) {
        try {
            return pushCommand.call()
        } catch (GitAPIException e) {
            throw new ScmException(e)
        }
    }

    private void verifyPushResults(Iterable<PushResult> pushResults) {
        PushResult pushResult = pushResults.iterator().next()
        Iterator<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates().iterator()

        remoteUpdates
            .find { it.getStatus() != RemoteRefUpdate.Status.OK && it.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE }
            ?.each { RemoteRefUpdate it ->  throw new ScmException(String.format("Push to SCM failed with message [%s]", it.message)) }
    }

    private PushCommand pushCommand(ScmIdentity identity, String remoteName, boolean all) {
        PushCommand push = repository.repository.jgit.push()
        push.remote = remoteName

        if (all) {
            push.setPushAll()
        }

        if (identity.privateKeyBased || identity.usernameBased) {
            setTransportOptions(identity, push)
        }

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
        if (!patterns.isEmpty()) {
            String canonicalPath = Pattern.quote(repositoryDir.canonicalPath + File.separatorChar)
            repository.add(patterns: patterns.collect { it.replaceFirst(canonicalPath, '') })
        }
        repository.commit(message: message)
    }

    ScmPosition currentPosition() {
        String revision = ''
        String shortRevision = ''
        if (hasCommits()) {
            Commit head = repository.head()
            revision = head.id
            shortRevision = head.abbreviatedId
        }
        return new ScmPosition(
            revision,
            shortRevision,
            repository.branch.current.name
        )
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern) {
        return latestTagsInternal(pattern, null, true)
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern, String sinceCommit) {
        return latestTagsInternal(pattern, sinceCommit, false)
    }

    private TagsOnCommit latestTagsInternal(Pattern pattern, String maybeSinceCommit, boolean inclusive) {
        List<TagsOnCommit> taggedCommits = taggedCommitsInternal(pattern, maybeSinceCommit, inclusive, true)
        return taggedCommits.isEmpty() ? TagsOnCommit.empty() : taggedCommits[0]
    }

    @Override
    List<TagsOnCommit> taggedCommits(Pattern pattern) {
        return taggedCommitsInternal(pattern, null, true, false)
    }

    private List<TagsOnCommit> taggedCommitsInternal(Pattern pattern,
                                                     String maybeSinceCommit,
                                                     boolean inclusive,
                                                     boolean stopOnFirstTag) {
        List<TagsOnCommit> taggedCommits = new ArrayList<>()
        if (!hasCommits()) {
            return taggedCommits
        }

        ObjectId headId = repository.repository.jgit.repository.resolve(Constants.HEAD)

        ObjectId startingCommit
        if (maybeSinceCommit != null) {
            startingCommit = ObjectId.fromString(maybeSinceCommit)
        } else {
            startingCommit = headId
        }

        RevWalk walk = walker(startingCommit)
        if (!inclusive) {
            walk.next()
        }

        Map<String, List<String>> allTags = tagsMatching(pattern, walk)

        RevCommit currentCommit
        List<String> currentTagsList
        for (currentCommit = walk.next(); currentCommit != null; currentCommit = walk.next()) {
            currentTagsList = allTags[currentCommit.id.name]
            if (currentTagsList) {
                TagsOnCommit taggedCommit = new TagsOnCommit(currentCommit.id.name(), currentTagsList, Objects.equals(currentCommit.id, headId))
                taggedCommits.add(taggedCommit)
                if (stopOnFirstTag) {
                    break
                }
            }
        }
        walk.dispose()

        return taggedCommits
    }

    private RevWalk walker(ObjectId startingCommit) {
        RevWalk walk = new RevWalk(repository.repository.jgit.repository)

        // explicitly set to NONE
        // TOPO sorting forces all commits in repo to be read in memory,
        // making walk incredibly slow
        walk.sort(RevSort.NONE)
        RevCommit head = walk.parseCommit(startingCommit)
        walk.markStart(head)
        return walk
    }

    private Map<String, List<String>> tagsMatching(Pattern pattern, RevWalk walk) {
        List<Ref> tags = repository.repository.jgit.tagList().call()
        return tags
            .collect({ tag -> [id: walk.parseCommit(tag.objectId).name, name: tag.name.substring(GIT_TAG_PREFIX.length())] })
            .grep({ tag -> tag.name ==~ pattern })
            .inject([:].withDefault({ p -> [] }), { map, entry ->
                map[entry.id] << entry.name
                return map
            })
    }

    private boolean hasCommits() {
        LogCommand log = repository.repository.jgit.log()
        log.maxCount = 1

        try {
            log.call()
            return true
        }
        catch (NoHeadException exception) {
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
