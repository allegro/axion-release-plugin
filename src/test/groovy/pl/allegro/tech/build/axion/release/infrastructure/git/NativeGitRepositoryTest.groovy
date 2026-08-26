package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.lib.Constants
import pl.allegro.tech.build.axion.release.domain.scm.ScmException
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit
import pl.allegro.tech.build.axion.release.util.WithEnvironment
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern

import static java.util.regex.Pattern.compile
import static pl.allegro.tech.build.axion.release.TagPrefixConf.defaultPrefix
import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

/**
 * Mirrors the read path scenarios of {@link GitRepositoryTest} and asserts that the native backend
 * returns exactly what the JGit backend returns for the same fixture repository.
 */
class NativeGitRepositoryTest extends Specification {

    private static final List<Pattern> ANY_PREFIXED_TAG = List.of(compile('^' + defaultPrefix() + '.*'))

    File repositoryDir

    File remoteRepositoryDir

    Grgit rawRepository

    GitRepository remoteRepository

    GitRepository jgitRepository

    NativeGitRepository repository

    String defaultBranch

    void setup() {
        remoteRepositoryDir = File.createTempDir('axion-release', 'tmp')
        Map remoteRepositories = GitProjectBuilder.gitProject(remoteRepositoryDir).withInitialCommit().build()
        remoteRepository = remoteRepositories[GitRepository]

        repositoryDir = File.createTempDir('axion-release', 'tmp')
        Map repositories = GitProjectBuilder.gitProject(repositoryDir, remoteRepositoryDir).build()

        rawRepository = repositories[Grgit]
        jgitRepository = repositories[GitRepository]
        repository = repositories[NativeGitRepository]

        defaultBranch = rawRepository.branch.current().name
    }

    def "should return last tag in current position in simple case"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'commit after release')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0']
        tags.commitId == jgitRepository.latestTags(ANY_PREFIXED_TAG).commitId
    }

    def "should return no tags when no commit in repository"() {
        given:
        NativeGitRepository commitlessRepository = GitProjectBuilder
            .gitProject(File.createTempDir('axion-release', 'tmp'))
            .build()[NativeGitRepository]

        when:
        TagsOnCommit tags = commitlessRepository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == []
    }

    def "should return no tags when repository has no matching tags"() {
        given:
        repository.tag('otherTag')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == []
    }

    def "should indicate that position is on tag when latest commit is tagged"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0']
        tags.commitId == rawRepository.head().id
    }

    def "should not peel lightweight tags"() {
        given:
        Map repositories = GitProjectBuilder.gitProject(File.createTempDir('axion-release', 'tmp'))
            .withInitialCommit()
            .withLightweightTag(fullPrefix() + '1.0.0')
            .build()
        NativeGitRepository lightweightTagRepository = repositories[NativeGitRepository]

        when:
        lightweightTagRepository.tag(fullPrefix() + '2.0.0')
        TagsOnCommit tags = lightweightTagRepository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0', fullPrefix() + '2.0.0']
    }

    def "should track back to older tag when commit was made after checking out older version"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '1')
        repository.tag(fullPrefix() + '2.0.0')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '2')

        rawRepository.checkout(branch: fullPrefix() + '1.0.0')
        repository.commit(['*'], 'bugfix after ' + fullPrefix() + '1')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0']
    }

    def "should return all tagged commits matching the pattern provided"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '1')
        repository.tag(fullPrefix() + '2.0.0')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '2')
        repository.tag('another-tag-1')
        repository.commit(['*'], 'commit after another-tag-1')
        repository.commit(['*'], 'commit after another-tag-1-2')
        repository.tag(fullPrefix() + '4')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '4')
        repository.tag(fullPrefix() + '3')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '3')

        when:
        List<TagsOnCommit> allTaggedCommits = repository.taggedCommits(ANY_PREFIXED_TAG)

        then:
        allTaggedCommits.collect { c -> c.tags[0] } == [fullPrefix() + '3', fullPrefix() + '4', fullPrefix() + '2.0.0', fullPrefix() + '1.0.0']
        allTaggedCommits.collect { c -> c.commitId } == jgitRepository.taggedCommits(ANY_PREFIXED_TAG).collect { c -> c.commitId }
    }

    def "should return only tags that match with prefix"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'commit after ' + fullPrefix() + '1')
        repository.tag('otherTag')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0']
    }

    def "should return latest tagged commit before the given commit id"() {
        given:
        repository.tag('tag-to-find')
        repository.commit(['*'], 'some commit')
        repository.tag('tag-to-skip')

        String latestCommitId = repository.latestTags(List.of(~'^tag.*')).commitId

        when:
        TagsOnCommit tags = repository.latestTags(List.of(~'^tag.*'), latestCommitId)

        then:
        tags.tags == ['tag-to-find']
        tags.commitId == jgitRepository.latestTags(List.of(~'^tag.*'), latestCommitId).commitId
    }

    def "should return list of tags when multiple matching tags found on same commit"() {
        given:
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() + '2.0.0')

        when:
        TagsOnCommit tags = repository.latestTags(ANY_PREFIXED_TAG)

        then:
        tags.tags == [fullPrefix() + '1.0.0', fullPrefix() + '2.0.0']
    }

    def "should provide current branch name and commit id in position"() {
        given:
        rawRepository.checkout(branch: 'some-branch', createBranch: true)
        repository.commit(['*'], 'first commit')

        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'some-branch'
        position.revision == rawRepository.head().id
        position.isClean == jgitRepository.currentPosition().isClean
    }

    def "should provide current branch name as HEAD when in detached state and overriddenBranchName not set"() {
        given:
        checkoutDetachedHead(rawRepository)

        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'HEAD'
    }

    def "should provide current branch name from overriddenBranchName when in detached state"() {
        given:
        File overriddenRepositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(overriddenRepositoryDir)
            .withOverriddenBranchName('refs/heads/feature/overridden-branch-name')
            .build()
        Map repositories = GitProjectBuilder.gitProject(overriddenRepositoryDir, remoteRepositoryDir)
            .usingProperties(scmProperties)
            .build()
        checkoutDetachedHead(repositories[Grgit] as Grgit)

        when:
        ScmPosition position = (repositories[NativeGitRepository] as NativeGitRepository).currentPosition()

        then:
        position.branch == 'feature/overridden-branch-name'
    }

    def "should not ignore overriddenBranchName when not in detached state"() {
        given:
        File overriddenRepositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(overriddenRepositoryDir)
            .withOverriddenBranchName('refs/heads/feature/overridden-branch-name')
            .build()
        Map repositories = GitProjectBuilder.gitProject(overriddenRepositoryDir, remoteRepositoryDir)
            .usingProperties(scmProperties)
            .build()
        (repositories[Grgit] as Grgit).checkout(branch: 'some-branch', createBranch: true)

        when:
        ScmPosition position = (repositories[NativeGitRepository] as NativeGitRepository).currentPosition()

        then:
        position.branch == 'feature/overridden-branch-name'
    }

    def "should signal there are uncommitted changes"() {
        when:
        new File(repositoryDir, 'uncommitted').createNewFile()

        then:
        repository.checkUncommittedChanges()
        jgitRepository.checkUncommittedChanges()
    }

    def "should not signal uncommitted changes on clean repository"() {
        expect:
        !repository.checkUncommittedChanges()
    }

    def "should see tags created after an earlier read"() {
        given:
        repository.latestTags(ANY_PREFIXED_TAG)

        when:
        repository.tag(fullPrefix() + '1.0.0')

        then:
        repository.latestTags(ANY_PREFIXED_TAG).tags == [fullPrefix() + '1.0.0']
    }

    def "should see working tree changes made after an earlier read"() {
        given:
        assert !repository.checkUncommittedChanges()

        when:
        new File(repositoryDir, 'uncommitted').createNewFile()

        then:
        repository.checkUncommittedChanges()
    }

    def "should respect overriddenIsClean-flag"(boolean expectedIsClean, Boolean overriddenIsCleanFlag, boolean dirtyRepository) {
        given:
        File overriddenRepositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(overriddenRepositoryDir)
            .withOverriddenIsClean(overriddenIsCleanFlag)
            .build()
        Map repositories = GitProjectBuilder.gitProject(overriddenRepositoryDir, remoteRepositoryDir)
            .usingProperties(scmProperties)
            .build()

        when:
        def dirtyFile = Path.of(overriddenRepositoryDir.path).resolve('dirty-file')
        if (dirtyRepository) {
            Files.createFile(dirtyFile)
        }
        ScmPosition position = (repositories[NativeGitRepository] as NativeGitRepository).currentPosition()

        then:
        position.isClean == expectedIsClean

        where:
        expectedIsClean | overriddenIsCleanFlag | dirtyRepository
        false           | false                 | false
        true            | true                  | false
        false           | false                 | true
        true            | true                  | true
        true            | null                  | false
        false           | null                  | true
    }

    def "last position with changes in subdir should work with backslashes"() {
        given:
        String subdirA = 'a/aa'
        String fileInA = "${subdirA}/foo"
        new File(repositoryDir, subdirA).mkdirs()
        new File(repositoryDir, fileInA).createNewFile()
        repository.commit([fileInA], 'Add file foo in subdirA')
        String headSubDirAChanged = rawRepository.head().id

        String subdirB = 'b/ba'
        String fileInB = "${subdirB}/bar"
        new File(repositoryDir, subdirB).mkdirs()
        new File(repositoryDir, fileInB).createNewFile()
        repository.commit([fileInB], 'Add file bar in subdirB')

        when:
        ScmPosition position = repository.positionOfLastChangeIn('a\\aa', [], [].toSet())

        then:
        position.revision == headSubDirAChanged
    }

    def "last position with monorepo dependency config - change made in dependency folder"() {
        given:
        String importantDir = 'a/aa'
        String dependencyDir = 'b/bb'
        String notInterestingDir = 'c/cc'

        commitFile(notInterestingDir, 'unintresting1')
        commitFile(importantDir, 'main_dir')
        commitFile(dependencyDir, 'dep_dir')
        String headSubDirAChanged = rawRepository.head().id

        commitFile(notInterestingDir, 'non_intresting')
        commitFile('after/aa', 'after')

        when:
        ScmPosition position = repository.positionOfLastChangeIn(importantDir, [], [dependencyDir].toSet())

        then:
        position.revision == headSubDirAChanged
        position.revision == jgitRepository.positionOfLastChangeIn(importantDir, [], [dependencyDir].toSet()).revision
    }

    def "last position in root should skip excluded subfolders"() {
        given:
        commitFile('a/aa', 'foo')
        String headRootChanged = rawRepository.head().id
        commitFile('excluded', 'bar')

        when:
        ScmPosition position = repository.positionOfLastChangeIn('', ['excluded'], [].toSet())

        then:
        position.revision == headRootChanged
        position.revision == jgitRepository.positionOfLastChangeIn('', ['excluded'], [].toSet()).revision
    }

    def "should fail when asked for position of non existing path"() {
        when:
        repository.positionOfLastChangeIn('not-there', [], [].toSet())

        then:
        thrown(ScmException)
    }

    def "should detect identical content for path between two revisions"() {
        given:
        commitFile('a/aa', 'foo')
        String taggedCommit = rawRepository.head().id
        commitFile('b/bb', 'bar')
        String headCommit = rawRepository.head().id

        expect:
        repository.isIdenticalForPath('a/aa', headCommit, taggedCommit)
        !repository.isIdenticalForPath('b/bb', headCommit, taggedCommit)
        !repository.isIdenticalForPath('a/aa', '', taggedCommit)
        repository.isIdenticalForPath('a/aa', headCommit, headCommit)
    }

    def "should pass ahead of remote check when in sync with remote"() {
        expect:
        repository.numberOfCommitsAheadOrBehindRemote() == 0
        jgitRepository.numberOfCommitsAheadOrBehindRemote() == 0
    }

    def "should report commits behind remote"() {
        given:
        remoteRepository.commit(['*'], 'remote commit')
        repository.fetchTags(ScmIdentity.defaultIdentityWithoutAgents(), 'origin')

        expect:
        repository.numberOfCommitsAheadOrBehindRemote() < 0
        repository.numberOfCommitsAheadOrBehindRemote() == jgitRepository.numberOfCommitsAheadOrBehindRemote()
    }

    def "should report commits ahead of remote"() {
        given:
        repository.commit(['*'], 'local commit')

        expect:
        repository.numberOfCommitsAheadOrBehindRemote() > 0
        repository.numberOfCommitsAheadOrBehindRemote() == jgitRepository.numberOfCommitsAheadOrBehindRemote()
    }

    def "should fail ahead of remote check when on branch with no remote tracking"() {
        given:
        NativeGitRepository noRemoteRepository = GitProjectBuilder
            .gitProject(File.createTempDir('axion-release', 'tmp'))
            .withInitialCommit()
            .build()[NativeGitRepository]

        when:
        noRemoteRepository.numberOfCommitsAheadOrBehindRemote()

        then:
        thrown(ScmException)
    }

    def "should detect remote by name"() {
        expect:
        repository.remoteAttached('origin')
        !repository.remoteAttached('notThere')
    }

    def "existing legacy default tagname repo should return true on all matches"() {
        given:
        repository.tag('release-1')
        repository.tag('release-9')

        expect:
        repository.isLegacyDefTagnameRepo()
    }

    def "existing legacy default tagname repo should return false on partial matches"() {
        given:
        repository.tag('release-1')
        repository.tag('bla1')

        expect:
        !repository.isLegacyDefTagnameRepo()
    }

    def "existing legacy default tagname repo should return false on empty matches"() {
        expect:
        !repository.isLegacyDefTagnameRepo()
    }

    def "existing legacy default tagname repo should return false on current default tag matches"() {
        given:
        repository.tag(fullPrefix() + '1')

        expect:
        !repository.isLegacyDefTagnameRepo()
    }

    def "should return last log messages"() {
        given:
        repository.commit(['*'], 'release version: 3.0.0')

        expect:
        repository.lastLogMessages(1) == ['release version: 3.0.0']
        repository.lastLogMessages(2) == jgitRepository.lastLogMessages(2)
    }

    @WithEnvironment([
        'GITHUB_ACTIONS=true',
        'GITHUB_HEAD_REF=pr-source-branch'
    ])
    def 'should get branch name on Github Actions if pull_request triggered the workflow'() {
        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'pr-source-branch'
    }

    @WithEnvironment([
        'GITHUB_ACTIONS=true',
        'GITHUB_HEAD_REF='
    ])
    def 'should ignore GITHUB_HEAD_REF variable if it has empty value'() {
        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == defaultBranch
    }

    private static void checkoutDetachedHead(Grgit grgit) {
        String headCommitId = grgit.repository.jgit.repository.resolve(Constants.HEAD).name()
        grgit.repository.jgit.checkout().setName(headCommitId).call()
    }

    private void commitFile(String subDir, String fileName) {
        String fileInA = "${subDir}/${fileName}"
        new File(repositoryDir, subDir).mkdirs()
        new File(repositoryDir, fileInA).createNewFile()
        repository.commit([fileInA], "Add file ${fileName} in ${subDir}")
    }
}
