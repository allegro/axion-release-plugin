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
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.TagOpt
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.URIish

import groovyjarjarantlr.StringUtils
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.scm.*

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
    void push(ScmIdentity identity, ScmPushOptions pushOptions) {
        push(identity, pushOptions, false)
    }

    void push(ScmIdentity identity, ScmPushOptions pushOptions, boolean all) {
        identity.useDefault ? callPush(pushOptions, all) : callLowLevelPush(identity, pushOptions, all)
    }

    private void callPush(ScmPushOptions pushOptions, boolean all) {
        if (!pushOptions.pushTagsOnly) {
            repository.push(remote: pushOptions.remote, all: all)
        }
        repository.push(remote: pushOptions.remote, tags: true, all: all)
    }

    private void callLowLevelPush(ScmIdentity identity, ScmPushOptions pushOptions, boolean all) {
        if (!pushOptions.pushTagsOnly) {
            pushCommand(identity, pushOptions.remote, all).call()
        }
        pushCommand(identity, pushOptions.remote, all).setPushTags().call()
    }

    private PushCommand pushCommand(ScmIdentity identity, String remoteName, boolean all) {
        PushCommand push = repository.repository.jgit.push()
        push.remote = remoteName

        if (all) {
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
		
		boolean isHeadCommit(String commitId) {
			if(commitId?.trim()) {
				ObjectId headId = repository.repository.jgit.repository.resolve(Constants.HEAD)
				return Objects.equals(ObjectId.fromString(commitId), headId)
			}
			return false;
		}
		
		LinkedHashMap<String, List<String>> allTaggedCommits(Pattern pattern, String maybeSinceCommit, boolean inclusive) {
			LinkedHashMap<String, List<String>> allTaggedCommits = new ArrayList<>();
			if (!hasCommits()) {
					return allTaggedCommits
			}

			Map<String, List<String>> allTags = tagsMatching(pattern)

			ObjectId headId = repository.repository.jgit.repository.resolve(Constants.HEAD)

			ObjectId startingCommit;
			if (maybeSinceCommit != null) {
					startingCommit = ObjectId.fromString(maybeSinceCommit)
			} else {
					startingCommit = headId
			}

			RevWalk walk = walker(startingCommit)
			if (!inclusive) {
					walk.next()
			}

			List tagsList = null

			RevCommit currentCommit
			List<String> currentTagNameList = null
			for (currentCommit = walk.next(); currentCommit != null; currentCommit = walk.next()) {
					currentTagNameList = allTags[currentCommit.id.name()]
					if (currentTagNameList) {
						allTaggedCommits.putAt(currentCommit.id.name(), currentTagNameList)
					}
			}
			walk.dispose()
			return allTaggedCommits
		}

    TagsOnCommit latestTags(Pattern pattern) {
        return latestTagsInternal(pattern, null, true)
    }

    TagsOnCommit latestTags(Pattern pattern, String sinceCommit) {
        return latestTagsInternal(pattern, sinceCommit, false)
    }

    private TagsOnCommit latestTagsInternal(Pattern pattern, String maybeSinceCommit, boolean inclusive) {
        if (!hasCommits()) {
            return new TagsOnCommit(null, [], false)
        }

        Map<String, List<String>> allTags = tagsMatching(pattern)

        ObjectId headId = repository.repository.jgit.repository.resolve(Constants.HEAD)

        ObjectId startingCommit;
        if (maybeSinceCommit != null) {
            startingCommit = ObjectId.fromString(maybeSinceCommit)
        } else {
            startingCommit = headId
        }

        RevWalk walk = walker(startingCommit)
        if (!inclusive) {
            walk.next()
        }

        List tagsList = null

        RevCommit commit
        for (commit = walk.next(); commit != null; commit = walk.next()) {
          tagsList = allTags[commit.id.name()]
          if (tagsList) {
              break
          }
      }
        walk.dispose()

        if (commit == null) {
            return new TagsOnCommit(null, [], false)
        }

        return new TagsOnCommit(commit.id.name(), tagsList, Objects.equals(commit.id, headId))
    }

    private RevWalk walker(ObjectId startingCommit) {
        RevWalk walk = new RevWalk(repository.repository.jgit.repository)
        walk.sort(RevSort.TOPO)
        walk.sort(RevSort.COMMIT_TIME_DESC, true)
        RevCommit head = walk.parseCommit(startingCommit)
        walk.markStart(head)
        return walk
    }

    private Map<String, List<String>> tagsMatching(Pattern pattern) {
        return repository.tag.list()
                .collect({ tag -> [id: tag.commit.id, name: tag.fullName.substring(GIT_TAG_PREFIX.length())] })
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
