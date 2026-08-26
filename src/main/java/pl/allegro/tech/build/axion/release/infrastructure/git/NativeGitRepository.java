package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.gradle.api.provider.ProviderFactory;
import org.gradle.process.ExecOutput;
import pl.allegro.tech.build.axion.release.domain.scm.ScmException;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullLegacyPrefix;

/**
 * {@link ScmRepository} that reads through the native {@code git} executable.
 * <p>
 * Only reads are native - on big repositories the JGit tag walk and status scan dominate configuration time.
 * Writes (tag/push/fetch/commit/remote) are delegated to a JGit backed repository so that transport
 * authentication driven by {@link ScmIdentity} keeps working unchanged.
 */
public class NativeGitRepository implements ScmRepository {

    private static final String GIT_TAG_PREFIX = "refs/tags/";

    /**
     * {@code %(*objectname)} is empty for lightweight tags and holds the peeled commit for annotated ones.
     */
    private static final String TAG_FORMAT = "--format=%(objectname) %(*objectname) %(refname:strip=2)";

    private static final String NULL_DEVICE = ProcessBuilder.Redirect.DISCARD.file().getPath();

    private final File repositoryDir;
    private final ScmProperties properties;
    private final ScmRepository writeDelegate;
    private final ProviderFactory providers;

    public NativeGitRepository(ScmProperties properties, ScmRepository writeDelegate) {
        this.properties = properties;
        this.repositoryDir = properties.getDirectory();
        this.writeDelegate = writeDelegate;
        this.providers = properties.getProviders();
    }

    @Override
    public ScmPosition currentPosition() {
        String revision = getRevision();
        String branchName = branchName();
        boolean isClean = !checkUncommittedChanges();
        boolean isReleaseBranch = properties.getReleaseBranchNames() != null && properties.getReleaseBranchNames().contains(branchName);
        return new ScmPosition(revision, branchName, isClean, isReleaseBranch);
    }

    @Override
    public ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders, Set<String> dependenciesFolders) {
        List<String> arguments = new ArrayList<>(Arrays.asList("log", "--max-count=1", "--format=%H"));

        // if the path is empty ('') then it means we are at the root of the Git directory
        // in which case, we should exclude changes that occurred in subdirectory projects when deciding on
        // which is the latest change that is relevant to the root project
        if (path.isEmpty()) {
            if (!excludeSubFolders.isEmpty()) {
                arguments.add("--");
                arguments.add(".");
                for (String excludedPath : excludeSubFolders) {
                    arguments.add(":(exclude)" + asUnixPath(excludedPath));
                }
            }
        } else {
            String unixStylePath = asUnixPath(path);
            assertPathExists(unixStylePath);
            arguments.add("--");
            arguments.add(unixStylePath);
            for (String dependencyFolder : dependenciesFolders) {
                arguments.add(asUnixPath(dependencyFolder));
            }
        }

        String lastCommit = git(arguments);
        ScmPosition currentPosition = currentPosition();

        if (lastCommit.isEmpty()) {
            return currentPosition;
        }

        return new ScmPosition(
            lastCommit,
            currentPosition.getBranch(),
            currentPosition.getIsClean(),
            currentPosition.getIsReleaseBranch()
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

        List<String> arguments = new ArrayList<>(Arrays.asList("diff", "--quiet", latestChangeRevision, tagCommitRevision));
        if (path != null && !path.isEmpty()) {
            arguments.add("--");
            arguments.add(asUnixPath(path));
        }

        ProcessOutput output = run(arguments);
        if (output.exitCode == 0) {
            return true;
        }
        if (output.exitCode == 1) {
            return false;
        }
        throw failure(arguments, output);
    }

    @Override
    public TagsOnCommit latestTags(List<Pattern> patterns) {
        return latestTagsInternal(patterns, null, true);
    }

    @Override
    public TagsOnCommit latestTags(List<Pattern> patterns, String sinceCommit) {
        return latestTagsInternal(patterns, sinceCommit, false);
    }

    @Override
    public List<TagsOnCommit> taggedCommits(List<Pattern> patterns) {
        return taggedCommitsInternal(patterns, null, true, false);
    }

    private TagsOnCommit latestTagsInternal(List<Pattern> patterns, String maybeSinceCommit, boolean inclusive) {
        List<TagsOnCommit> taggedCommits = taggedCommitsInternal(patterns, maybeSinceCommit, inclusive, true);
        return taggedCommits.isEmpty() ? TagsOnCommit.empty() : taggedCommits.get(0);
    }

    private List<TagsOnCommit> taggedCommitsInternal(
        List<Pattern> patterns,
        String maybeSinceCommit,
        boolean inclusive,
        boolean stopOnFirstTag
    ) {
        List<TagsOnCommit> taggedCommits = new ArrayList<>();

        Map<String, List<String>> allTags = tagsMatching(patterns);
        if (allTags.isEmpty() || !hasCommits()) {
            return taggedCommits;
        }

        String startingCommit = maybeSinceCommit == null ? "HEAD" : maybeSinceCommit;
        walkCommits(startingCommit, inclusive, commitId -> {
            List<String> tagsOnCommit = allTags.get(commitId);
            if (tagsOnCommit == null) {
                return true;
            }
            taggedCommits.add(new TagsOnCommit(commitId, tagsOnCommit));
            return !stopOnFirstTag;
        });

        return taggedCommits;
    }

    /**
     * Peeled commit id to matching tag names. {@code for-each-ref} sorts by ref name, same as JGit's tag list.
     */
    private Map<String, List<String>> tagsMatching(List<Pattern> patterns) {
        Map<String, List<String>> tags = new HashMap<>();

        for (String line : lines(git(Arrays.asList("for-each-ref", TAG_FORMAT, "refs/tags")))) {
            String[] parts = line.split(" ", 3);
            if (parts.length < 3) {
                continue;
            }
            String name = parts[2];
            if (patterns.stream().noneMatch(pattern -> pattern.matcher(name).matches())) {
                continue;
            }
            String commitId = parts[1].isEmpty() ? parts[0] : parts[1];
            tags.computeIfAbsent(commitId, id -> new ArrayList<>()).add(name);
        }

        return tags;
    }

    /**
     * Commits are emitted in commit date order, which is what JGit's {@code RevSort.NONE} walk does as well.
     *
     * @param inclusive when false the starting commit itself is skipped
     * @param visitor   returns false to stop the walk
     */
    private void walkCommits(String startingCommit, boolean inclusive, Predicate<String> visitor) {
        List<String> commits = lines(git(Arrays.asList("rev-list", startingCommit)));
        for (int i = inclusive ? 0 : 1; i < commits.size(); i++) {
            if (!visitor.test(commits.get(i))) {
                return;
            }
        }
    }

    @Override
    public boolean remoteAttached(String remoteName) {
        return lines(git(Collections.singletonList("remote"))).contains(remoteName);
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
        return !git(Arrays.asList("status", "--porcelain")).isEmpty();
    }

    @Override
    public int numberOfCommitsAheadOrBehindRemote() {
        ProcessOutput upstream = run(Arrays.asList("rev-parse", "--symbolic-full-name", "@{upstream}"));
        if (upstream.exitCode != 0 || upstream.standardOutput.trim().isEmpty()) {
            throw new ScmException("Branch " + fullBranchName() + " is not set to track another branch");
        }

        String range = upstream.standardOutput.trim() + "...HEAD";
        String[] counts = git(Arrays.asList("rev-list", "--left-right", "--count", range)).split("\\s+");
        int behind = Integer.parseInt(counts[0]);
        int ahead = Integer.parseInt(counts[1]);

        if (ahead > 0) {
            return ahead;
        }
        if (behind > 0) {
            return -behind;
        }
        return 0;
    }

    @Override
    public boolean isLegacyDefTagnameRepo() {
        List<String> tags = lines(git(Arrays.asList("for-each-ref", "--format=%(refname)", "refs/tags")));
        if (tags.isEmpty()) {
            return false;
        }
        return tags.stream().allMatch(ref -> ref.startsWith(GIT_TAG_PREFIX + fullLegacyPrefix()));
    }

    @Override
    public List<String> lastLogMessages(int messageCount) {
        String messages = git(Arrays.asList("log", "--max-count=" + messageCount, "--format=%B%x00"));
        return Arrays.stream(messages.split("\u0000"))
            .map(String::trim)
            .filter(message -> !message.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public void fetchTags(ScmIdentity identity, String remoteName) {
        writeDelegate.fetchTags(identity, remoteName);
    }

    @Override
    public void tag(String tagName, String tagMessage) {
        writeDelegate.tag(tagName, tagMessage);
    }

    @Override
    public void dropTag(String tagName) {
        writeDelegate.dropTag(tagName);
    }

    @Override
    public ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions) {
        return writeDelegate.push(identity, pushOptions);
    }

    @Override
    public void commit(List<String> patterns, String message) {
        writeDelegate.commit(patterns, message);
    }

    @Override
    public void attachRemote(String remoteName, String url) {
        writeDelegate.attachRemote(remoteName, url);
    }

    public ScmRepository getWriteDelegate() {
        return writeDelegate;
    }

    private String getRevision() {
        ProcessOutput output = run(Arrays.asList("rev-parse", "--verify", "--quiet", "HEAD"));
        return output.exitCode == 0 ? output.standardOutput.trim() : "";
    }

    private boolean hasCommits() {
        return !getRevision().isEmpty();
    }

    private String branchName() {
        return branchNameFromGithubEnvVariable().orElseGet(this::branchNameFromGit);
    }

    /**
     * @see GitRepository for the rationale behind reading GITHUB_HEAD_REF
     */
    private Optional<String> branchNameFromGithubEnvVariable() {
        if (onGithubActions()) {
            return env("GITHUB_HEAD_REF").filter(it -> !it.isBlank());
        }
        return Optional.empty();
    }

    /**
     * @return branch name or 'HEAD' when in detached state, unless it is overridden by 'overriddenBranchName'
     */
    private String branchNameFromGit() {
        String overriddenBranchName = properties.getOverriddenBranchName();
        if (overriddenBranchName != null && !overriddenBranchName.isEmpty()) {
            return shortenRefName(overriddenBranchName);
        }
        // unlike 'rev-parse --abbrev-ref' this also resolves branches without any commit yet
        ProcessOutput output = run(Arrays.asList("symbolic-ref", "--quiet", "--short", "HEAD"));
        return output.exitCode == 0 ? output.standardOutput.trim() : "HEAD";
    }

    private String fullBranchName() {
        ProcessOutput output = run(Arrays.asList("rev-parse", "--symbolic-full-name", "HEAD"));
        return output.exitCode == 0 ? output.standardOutput.trim() : "HEAD";
    }

    private static String shortenRefName(String refName) {
        if (refName.startsWith("refs/heads/")) {
            return refName.substring("refs/heads/".length());
        }
        if (refName.startsWith(GIT_TAG_PREFIX)) {
            return refName.substring(GIT_TAG_PREFIX.length());
        }
        if (refName.startsWith("refs/remotes/")) {
            return refName.substring("refs/remotes/".length());
        }
        return refName;
    }

    private boolean onGithubActions() {
        return env("GITHUB_ACTIONS").map(it -> it.equals("true")).orElse(false);
    }

    private Optional<String> env(String name) {
        return Optional.ofNullable(System.getenv(name));
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

    private static List<String> lines(String output) {
        return output.isEmpty() ? Collections.emptyList() : Arrays.asList(output.split("\\R"));
    }

    private String git(List<String> arguments) {
        ProcessOutput output = run(arguments);
        if (output.exitCode != 0) {
            throw failure(arguments, output);
        }
        return output.standardOutput.trim();
    }

    /**
     * Uses Gradle's exec provider rather than {@link ProcessBuilder}, so that reads performed during
     * configuration stay compatible with the configuration cache.
     */
    private ProcessOutput run(List<String> arguments) {
        List<String> command = new ArrayList<>(arguments.size() + 1);
        command.add("git");
        command.addAll(arguments);

        ExecOutput output = providers.exec(spec -> {
            spec.setCommandLine(command);
            spec.setWorkingDir(repositoryDir);
            spec.setIgnoreExitValue(true);
            // read only commands should never take index.lock, parallel builds would fight over it
            spec.environment("GIT_OPTIONAL_LOCKS", "0");
            if (properties.isIgnoreGlobalGitConfig()) {
                spec.environment("GIT_CONFIG_GLOBAL", NULL_DEVICE);
                spec.environment("GIT_CONFIG_SYSTEM", NULL_DEVICE);
            }
        });

        return new ProcessOutput(
            output.getResult().get().getExitValue(),
            output.getStandardOutput().getAsText().get(),
            output.getStandardError().getAsText().get()
        );
    }

    private static ScmException failure(List<String> arguments, ProcessOutput output) {
        return new ScmException(
            "git " + String.join(" ", arguments) + " failed with exit code " + output.exitCode
                + ": " + output.errorOutput.trim()
        );
    }

    private static final class ProcessOutput {
        final int exitCode;
        final String standardOutput;
        final String errorOutput;

        ProcessOutput(int exitCode, String standardOutput, String errorOutput) {
            this.exitCode = exitCode;
            this.standardOutput = standardOutput;
            this.errorOutput = errorOutput;
        }
    }
}
