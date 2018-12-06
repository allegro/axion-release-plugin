package pl.allegro.tech.build.axion.release.infrastructure.git

import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.*
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.scm.*

import java.util.regex.Pattern

class GitRepository implements ScmRepository {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(GitRepository)

    private static final String GIT_TAG_PREFIX = 'refs/tags/'

    private final TransportConfigFactory transportConfigFactory = new TransportConfigFactory()

    private final File repositoryDir

    private final Git jgitRepository

    private final ScmProperties properties

    GitRepository(ScmProperties properties) {
        try {
            this.repositoryDir = properties.directory
            this.jgitRepository = Git.open(repositoryDir)
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

    /**
     * This fetch method behaves like git fetch, meaning it only fetches thing without merging.
     * As a result, any fetched tags will not be visible via GitRepository tag listing methods
     * because they do commit-tree walk, not tag listing.
     *
     * This method is only useful if you have bare repo on CI systems, where merge is not neccessary, because newest
     * version of content has already been fetched.
     */
    @Override
    void fetchTags(ScmIdentity identity, String remoteName) {
        FetchCommand fetch = jgitRepository.fetch()
            .setRemote(remoteName)
            .setTagOpt(TagOpt.FETCH_TAGS)
            .setTransportConfigCallback(transportConfigFactory.create(identity))
        fetch.call()
    }

    @Override
    void tag(String tagName) {
        String headId = head().name()

        boolean isOnExistingTag = jgitRepository.tagList().call().any({
            it -> it.name == GIT_TAG_PREFIX + tagName && jgitRepository.repository.peel(it).peeledObjectId.name == headId
        })
        if (!isOnExistingTag) {
            jgitRepository.tag()
                .setName(tagName)
                .call()
        } else {
            logger.debug("The head commit $headId already has the tag $tagName.")
        }
    }

    private ObjectId head() {
        return jgitRepository.repository.resolve(Constants.HEAD)
    }

    @Override
    void dropTag(String tagName) {
        try {
            jgitRepository.tagDelete()
                .setTags(GIT_TAG_PREFIX + tagName)
                .call()
        } catch (GitAPIException e) {
            throw new ScmException(e)
        }
    }

    @Override
    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions) {
        return push(identity, pushOptions, false)
    }

    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions, boolean all) {
        PushCommand command = pushCommand(identity, pushOptions.remote, all)

        // command has to be called twice:
        // once for commits (only if needed)
        if (!pushOptions.pushTagsOnly) {
            ScmPushResult result = verifyPushResults(callPush(command))
            if (!result.success) {
                return result
            }
        }

        // and another time for tags
        return verifyPushResults(callPush(command.setPushTags()))
    }

    private Iterable<PushResult> callPush(PushCommand pushCommand) {
        try {
            return pushCommand.call()
        } catch (GitAPIException e) {
            throw new ScmException(e)
        }
    }

    private ScmPushResult verifyPushResults(Iterable<PushResult> pushResults) {
        PushResult pushResult = pushResults.iterator().next()
        Iterator<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates().iterator()

        RemoteRefUpdate failedRefUpdate = remoteUpdates.find({
            it.getStatus() != RemoteRefUpdate.Status.OK && it.getStatus() != RemoteRefUpdate.Status.UP_TO_DATE
        })

        return new ScmPushResult(failedRefUpdate == null, Optional.ofNullable(pushResult.messages))
    }

    private PushCommand pushCommand(ScmIdentity identity, String remoteName, boolean all) {
        PushCommand push = jgitRepository.push()
        push.remote = remoteName

        if (all) {
            push.setPushAll()
        }
        push.transportConfigCallback = transportConfigFactory.create(identity)

        return push
    }

    @Override
    void attachRemote(String remoteName, String remoteUrl) {
        Config config = jgitRepository.repository.config

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
            AddCommand command = jgitRepository.add()
            patterns.collect({ it.replaceFirst(canonicalPath, '') }).each({ command.addFilepattern(it) })
            command.call()
        }
        jgitRepository.commit()
            .setMessage(message)
            .call()
    }

    ScmPosition currentPosition() {
        String revision = ''
        String shortRevision = ''
        if (hasCommits()) {
            ObjectId head = head()
            revision = head.name()
            shortRevision = revision[0..(7 - 1)]
        }

        // this returns HEAD as branch name when in detached state
        String branchName = Optional.of(jgitRepository.repository.exactRef(Constants.HEAD)?.target.name)
            .map({s -> Repository.shortenRefName(s)})
            .orElse(null)

        return new ScmPosition(
            revision,
            shortRevision,
            branchName
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

        ObjectId headId = jgitRepository.repository.resolve(Constants.HEAD)

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
        RevWalk walk = new RevWalk(jgitRepository.repository)

        // explicitly set to NONE
        // TOPO sorting forces all commits in repo to be read in memory,
        // making walk incredibly slow
        walk.sort(RevSort.NONE)
        RevCommit head = walk.parseCommit(startingCommit)
        walk.markStart(head)
        return walk
    }

    private Map<String, List<String>> tagsMatching(Pattern pattern, RevWalk walk) {
        List<Ref> tags = jgitRepository.tagList().call()
        return tags
            .collect({ tag -> [id: walk.parseCommit(tag.objectId).name, name: tag.name.substring(GIT_TAG_PREFIX.length())] })
            .grep({ tag -> tag.name ==~ pattern })
            .inject([:].withDefault({ p -> [] }), { map, entry ->
            map[entry.id] << entry.name
            return map
        })
    }

    private boolean hasCommits() {
        LogCommand log = jgitRepository.log()
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
        Config config = jgitRepository.repository.config
        return config.getSubsections('remote').any { it == remoteName }
    }

    @Override
    boolean checkUncommittedChanges() {
        Status status = jgitRepository.status().call()
        logger.debug("""git status check:
            |  added:       ${status.added}
            |  changed:     ${status.changed}
            |  removed:     ${status.removed}
            |  missing:     ${status.missing}
            |  modified:    ${status.modified}
            |  conflicting: ${status.conflicting}
            |  untracked:   ${status.untracked}""".stripMargin())
        return !status.isClean()
    }

    @Override
    boolean checkAheadOfRemote() {
        String branchName = jgitRepository.repository.fullBranch
        BranchTrackingStatus status = BranchTrackingStatus.of(jgitRepository.repository, branchName)

        if (status == null) {
            throw new ScmException("Branch $branchName is not set to track another branch")
        }

        return status.aheadCount != 0 || status.behindCount != 0
    }

    Status listChanges() {
        return jgitRepository.status().call()
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        return jgitRepository.log()
            .setMaxCount(messageCount)
            .call()*.fullMessage
    }
}
