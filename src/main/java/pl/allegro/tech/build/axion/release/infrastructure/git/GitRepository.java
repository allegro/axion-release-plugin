package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.SystemReader;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import pl.allegro.tech.build.axion.release.domain.scm.ScmException;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResultOutcome;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException;
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullLegacyPrefix;

public class GitRepository implements ScmRepository {
    private static final Logger logger = Logging.getLogger(GitRepository.class);

    private static final String GIT_TAG_PREFIX = "refs/tags/";

    private final TransportConfigFactory transportConfigFactory = new TransportConfigFactory();
    private final File repositoryDir;
    private final Git jgitRepository;
    private final ScmProperties properties;

    public GitRepository(ScmProperties properties) {
        SystemReader.setInstance(new SystemReaderWithoutSystemConfig(properties.isIgnoreGlobalGitConfig()));
        try {
            this.repositoryDir = properties.getDirectory();
            this.jgitRepository = Git.open(repositoryDir);
            this.properties = properties;
        } catch (RepositoryNotFoundException exception) {
            throw new ScmRepositoryUnavailableException(exception);
        } catch (IOException exception) {
            throw new ScmException(exception);
        }

        if (properties.isAttachRemote()) {
            attachRemote(properties.getRemote(), properties.getRemoteUrl());
        }

        if (properties.isFetchTags()) {
            fetchTags(properties.getIdentity(), properties.getRemote());
        }

        if (onCI() && properties.isUnshallowRepoOnCI()) {
            unshallowRepo(properties.getIdentity());
        }
    }

    private void unshallowRepo(ScmIdentity identity) {
        try {
            logger.lifecycle("Unshallowing repo because the build is being executed on CI");
            jgitRepository.fetch()
                .setUnshallow(true)
                .setTransportConfigCallback(transportConfigFactory.create(identity))
                .call();
        } catch (GitAPIException e) {
            logger.warn("Unable to unshallow repo on GitHub actions, continuing with shallow repo", e);
        }
    }

    /**
     * This fetch method behaves like git fetch, meaning it only fetches thing without merging.
     * As a result, any fetched tags will not be visible via GitRepository tag listing methods
     * because they do commit-tree walk, not tag listing.
     * <p>
     * This method is only useful if you have bare repo on CI systems, where merge is not neccessary, because newest
     * version of content has already been fetched.
     */
    @Override
    public void fetchTags(ScmIdentity identity, String remoteName) {
        FetchCommand fetch = jgitRepository.fetch()
            .setRemote(remoteName)
            .setTagOpt(TagOpt.FETCH_TAGS)
            .setTransportConfigCallback(transportConfigFactory.create(identity));
        try {
            fetch.call();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public void tag(final String tagName) {
        try {
            final String headId = head().name();
            List<Ref> tags = jgitRepository.tagList().call();

            boolean isOnExistingTag = tags.stream().anyMatch(ref -> {
                boolean onTag = ref.getName().equals(GIT_TAG_PREFIX + tagName);
                boolean onHead;
                try {
                    ObjectId peeledObjectId = jgitRepository.getRepository().getRefDatabase()
                        .peel(ref).getPeeledObjectId();
                    if (peeledObjectId != null) {
                        onHead = peeledObjectId.getName().equals(headId);
                    } else {
                        onHead = ref.getName().equals(headId);
                        logger.debug("Using lightweight (not annotated) tag " + ref.getName() + ".");
                    }
                } catch (IOException e) {
                    throw new ScmException(e);
                }

                return onTag && onHead;
            });

            if (!isOnExistingTag) {
                jgitRepository.tag().setName(tagName).call();
            } else {
                logger.debug("The head commit " + headId + " already has the tag " + tagName + ".");
            }
        } catch (GitAPIException | IOException e) {
            throw new ScmException(e);
        }
    }

    private ObjectId head() throws IOException {
        return jgitRepository.getRepository().resolve(Constants.HEAD);
    }

    @Override
    public void dropTag(String tagName) {
        try {
            jgitRepository.tagDelete().setTags(GIT_TAG_PREFIX + tagName).call();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }

    }

    @Override
    public ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions) {
        return push(identity, pushOptions, false);
    }

    public ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions, boolean all) {
        // push once for commits (only if needed)
        if (!pushOptions.isPushTagsOnly()) {
            PushCommand command = pushCommand(identity, pushOptions.getRemote(), all);
            ScmPushResult result = verifyPushResults(callPush(command));
            if (result.getOutcome().equals(ScmPushResultOutcome.FAILED)) {
                return result;
            }
        }

        // and again for tags
        // Note: push commands can *not* be re-used.
        PushCommand command = pushCommand(identity, pushOptions.getRemote(), all);
        return verifyPushResults(callPush(command.setPushTags()));
    }

    private Iterable<PushResult> callPush(PushCommand pushCommand) {
        try {
            return pushCommand.call();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    private ScmPushResult verifyPushResults(Iterable<PushResult> pushResults) {
        PushResult pushResult = pushResults.iterator().next();

        Optional<RemoteRefUpdate> failedRefUpdate = pushResult.getRemoteUpdates().stream().filter(ref ->
            !ref.getStatus().equals(RemoteRefUpdate.Status.OK)
                && !ref.getStatus().equals(RemoteRefUpdate.Status.UP_TO_DATE)
        ).findFirst();

        boolean isSuccess = failedRefUpdate.isEmpty();
        Optional<RemoteRefUpdate.Status> failureCause = isSuccess ?
            Optional.empty() : Optional.of(failedRefUpdate.get().getStatus());

        return new ScmPushResult(
            isSuccess ? ScmPushResultOutcome.SUCCESS : ScmPushResultOutcome.FAILED,
            failureCause,
            Optional.ofNullable(pushResult.getMessages())
        );
    }

    private PushCommand pushCommand(ScmIdentity identity, String remoteName, boolean all) {
        PushCommand push = jgitRepository.push();
        push.setRemote(remoteName);

        if (all) {
            push.setPushAll();
        }

        push.setTransportConfigCallback(transportConfigFactory.create(identity));

        return push;
    }

    @Override
    public void attachRemote(String remoteName, String remoteUrl) {
        StoredConfig config = jgitRepository.getRepository().getConfig();

        try {
            RemoteConfig remote = new RemoteConfig(config, remoteName);
            // clear other push specs
            List<URIish> pushUris = new ArrayList<>(remote.getPushURIs());
            for (URIish uri : pushUris) {
                remote.removePushURI(uri);
            }

            remote.addPushURI(new URIish(remoteUrl));
            remote.update(config);

            config.save();
        } catch (URISyntaxException | IOException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public void commit(List<String> patterns, String message) {
        try {
            if (!patterns.isEmpty()) {
                String canonicalPath = Pattern.quote(repositoryDir.getCanonicalPath() + File.separatorChar);
                AddCommand command = jgitRepository.add();
                patterns.stream().map(p -> p.replaceFirst(canonicalPath, "")).forEach(command::addFilepattern);
                command.call();
            }

            jgitRepository.commit().setMessage(message).call();
        } catch (GitAPIException | IOException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders, Set<String> dependenciesFolders) {
        RevCommit lastCommit;

        // if the path is empty ('') then it means we are at the root of the Git directory
        // in which case, we should exclude changes that occurred in subdirectory projects when deciding on
        // which is the latest change that is relevant to the root project
        try {
            LogCommand logCommand;
            if (path.isEmpty()) {
                logCommand = jgitRepository.log().setMaxCount(1);
                for (String excludedPath : excludeSubFolders) {
                    logCommand.excludePath(asUnixPath(excludedPath));
                }
            } else {
                String unixStylePath = asUnixPath(path);
                assertPathExists(unixStylePath);
                logCommand = jgitRepository.log().setMaxCount(1).addPath(unixStylePath);
                for (String dep : dependenciesFolders) {
                    logCommand.addPath(asUnixPath(dep));
                }
            }
            lastCommit = logCommand.call().iterator().next();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }

        ScmPosition currentPosition = currentPosition();

        if (lastCommit == null) {
            return currentPosition;
        }

        return new ScmPosition(
            lastCommit.getName(),
            currentPosition.getBranch(),
            currentPosition.getIsClean()
        );
    }

    @Override
    public Boolean isIdenticalForPath(String path, String latestChangeRevision, String tagCommitRevision) {
        if (latestChangeRevision.isEmpty() || tagCommitRevision.isEmpty()) {
            return false;
        }
        if (latestChangeRevision.equals(tagCommitRevision)) {
            return true;
        }
        try {
            ObjectId lastChange = jgitRepository.getRepository().resolve(latestChangeRevision);
            ObjectId taggedCommit = jgitRepository.getRepository().resolve(tagCommitRevision);
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setPathFilter(PathFilter.create(asUnixPath(path)));
            diffFormatter.setRepository(jgitRepository.getRepository());
            return diffFormatter.scan(lastChange, taggedCommit).isEmpty();
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    private String asUnixPath(String path) {
        return path == null ? null : path.replaceAll("\\\\", "/");
    }

    private void assertPathExists(String path) {
        File subpath = new File(repositoryDir, path);
        if (!subpath.exists()) {
            throw new ScmException(
                String.format("Path '%s' does not exist in repository '%s'.",
                    path, repositoryDir.getAbsolutePath()
                ));
        }
    }

    public ScmPosition currentPosition() {
        String revision = getRevision();
        String branchName = branchName();
        boolean isClean = !checkUncommittedChanges();
        return new ScmPosition(revision, branchName, isClean);
    }

    private String getRevision() {
        try {
            String revision = "";
            if (hasCommits()) {
                ObjectId head = head();
                revision = head.name();
            }
            return revision;
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    private String branchName() {
        return branchNameFromGithubEnvVariable().orElseGet(this::branchNameFromGit);
    }

    /**
     * <p>If executed within workflow on GitHub Actions and that workflow is triggered by pull_request (or pull_request_target)
     * event, GitHub will not check out the source branch of the PR, but some custom ref: 'refs/pull/<pr_number>/merge'
     * (they call it a "merge branch"). It effectively means that repository is in detached-HEAD state and axion-release
     * is not able to get the branch name from HEAD ref.</p>
     *
     * <p>For pull_request and pull_request_target events, GitHub sets a special environment variable GITHUB_HEAD_REF.
     * It contains the head ref of source branch of the PR which triggered the workflow. This variable is not set
     * for other event types.</p>
     *
     * <p>If the build is not executed on GitHub Actions, this method will always return Optional.empty.</p>
     *
     * @return source branch of the PR which triggered GitHub Actions workflow
     */
    private Optional<String> branchNameFromGithubEnvVariable() {
        if (onGithubActions()) {
            return env("GITHUB_HEAD_REF")
                // GitHub violates its own documentation and sets this variable always. For pull_request and
                // pull_request_target it contains actual value, for every other event it's just empty string
                .filter(it -> !it.isBlank());
        }
        return Optional.empty();
    }

    private boolean onGithubActions() {
        return env("GITHUB_ACTIONS").map(it -> it.equals("true")).orElse(false);
    }

    private boolean onCI() {
        return env("CI").map(it -> it.equals("true")).orElse(false);
    }

    private Optional<String> env(String name) {
        return Optional.ofNullable(System.getenv(name));
    }

    /**
     * @return branch name or 'HEAD' when in detached state, unless it is overridden by 'overriddenBranchName'
     */
    private String branchNameFromGit() {
        try {
            // this returns HEAD as branch name when in detached state
            Optional<Ref> ref = Optional.ofNullable(jgitRepository.getRepository().exactRef(Constants.HEAD));
            String branchName = ref.map(r -> r.getTarget().getName())
                .map(Repository::shortenRefName)
                .orElse(null);

            if ("HEAD".equals(branchName) && properties.getOverriddenBranchName() != null && !properties.getOverriddenBranchName().isEmpty()) {
                branchName = Repository.shortenRefName(properties.getOverriddenBranchName());
            }
            return branchName;
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public TagsOnCommit latestTags(List<Pattern> patterns) {
        return latestTagsInternal(patterns, null, true);
    }

    @Override
    public TagsOnCommit latestTags(List<Pattern> patterns, String sinceCommit) {
        return latestTagsInternal(patterns, sinceCommit, false);
    }

    private TagsOnCommit latestTagsInternal(List<Pattern> patterns, String maybeSinceCommit, boolean inclusive) {
        List<TagsOnCommit> taggedCommits = taggedCommitsInternal(patterns, maybeSinceCommit, inclusive, true);
        return taggedCommits.isEmpty() ? TagsOnCommit.empty() : taggedCommits.get(0);
    }

    @Override
    public List<TagsOnCommit> taggedCommits(List<Pattern> patterns) {
        return taggedCommitsInternal(patterns, null, true, false);
    }

    private List<TagsOnCommit> taggedCommitsInternal(List<Pattern> patterns, String maybeSinceCommit, boolean inclusive, boolean stopOnFirstTag) {
        List<TagsOnCommit> taggedCommits = new ArrayList<>();
        if (!hasCommits()) {
            return taggedCommits;
        }

        try {
            ObjectId headId = jgitRepository.getRepository().resolve(Constants.HEAD);

            ObjectId startingCommit;
            if (maybeSinceCommit != null) {
                startingCommit = ObjectId.fromString(maybeSinceCommit);
            } else {
                startingCommit = headId;
            }


            RevWalk walk = walker(startingCommit);
            if (!inclusive) {
                walk.next();
            }

            Map<String, List<String>> allTags = tagsMatching(patterns, walk);

            while (true) {
                RevCommit currentCommit = walk.next();
                if (currentCommit == null) {
                    break;
                }

                List<String> currentTagsList = allTags.get(currentCommit.getId().getName());

                if (currentTagsList != null) {
                    TagsOnCommit taggedCommit = new TagsOnCommit(
                        currentCommit.getId().name(),
                        currentTagsList
                    );
                    taggedCommits.add(taggedCommit);

                    if (stopOnFirstTag) {
                        break;
                    }

                }

            }
            walk.dispose();
        } catch (IOException | GitAPIException e) {
            throw new ScmException(e);
        }

        return taggedCommits;
    }

    private RevWalk walker(ObjectId startingCommit) throws IOException {
        RevWalk walk = new RevWalk(jgitRepository.getRepository());

        // explicitly set to NONE
        // TOPO sorting forces all commits in repo to be read in memory,
        // making walk incredibly slow
        walk.sort(RevSort.NONE);
        RevCommit head = walk.parseCommit(startingCommit);
        walk.markStart(head);
        return walk;
    }

    private Map<String, List<String>> tagsMatching(List<Pattern> patterns, RevWalk walk) throws GitAPIException {
        List<Ref> tags = jgitRepository.tagList().call();

        return tags.stream()
            .map(tag -> new TagNameAndId(
                tag.getName().substring(GIT_TAG_PREFIX.length()),
                parseCommitSafe(walk, tag.getObjectId())
            ))
            .filter(t -> patterns.stream().anyMatch(pattern -> pattern.matcher(t.name).matches()))
            .collect(
                HashMap::new,
                (m, t) -> m.computeIfAbsent(t.id, (s) -> new ArrayList<>()).add(t.name),
                HashMap::putAll
            );
    }

    private String parseCommitSafe(RevWalk walk, AnyObjectId commitId) {
        try {
            return walk.parseCommit(commitId).getName();
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    private final static class TagNameAndId {
        final String name;
        final String id;

        TagNameAndId(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    private boolean hasCommits() {
        LogCommand log = jgitRepository.log();
        log.setMaxCount(1);

        try {
            log.call();
            return true;
        } catch (NoHeadException exception) {
            return false;
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public boolean remoteAttached(final String remoteName) {
        Config config = jgitRepository.getRepository().getConfig();
        return config.getSubsections("remote").stream().anyMatch(n -> n.equals(remoteName));
    }

    /**
     * @return true when there are uncommitted changes. This means: not clean
     */
    @Override
    public boolean checkUncommittedChanges() {
        Optional<Boolean> overriddenIsClean = properties.getOverriddenIsClean();
        if (overriddenIsClean.isPresent()) {
            // the logic to get the isClean-property is inverted
            return !overriddenIsClean.get();
        }
        try {
            return !jgitRepository.status().call().isClean();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public int numberOfCommitsAheadOrBehindRemote() {
        try {
            String branchName = jgitRepository.getRepository().getFullBranch();
            BranchTrackingStatus status = BranchTrackingStatus.of(jgitRepository.getRepository(), branchName);

            if (status == null) {
                throw new ScmException("Branch " + branchName + " is not set to track another branch");
            }

            if (status.getAheadCount() > 0) {
                return status.getAheadCount();
            }
            if (status.getBehindCount() > 0) {
                return -status.getBehindCount();
            }
            return 0;

        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    public Status listChanges() {
        try {
            return jgitRepository.status().call();
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public boolean isLegacyDefTagnameRepo() {
        try {
            List<Ref> call = jgitRepository.tagList().call();
            if (call.isEmpty()) return false;

            return call.stream().allMatch(ref -> ref.getName().startsWith("refs/tags/" + fullLegacyPrefix()));
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public List<String> lastLogMessages(int messageCount) {
        try {
            return StreamSupport.stream(jgitRepository.log().setMaxCount(messageCount).call().spliterator(), false)
                .map(RevCommit::getFullMessage)
                .collect(toList());
        } catch (GitAPIException e) {
            throw new ScmException(e);
        }
    }

    public Git getJgitRepository() {
        return jgitRepository;
    }
}
