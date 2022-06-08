package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.errors.RefAlreadyExistsException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.*
import spock.lang.Specification

import java.util.regex.Pattern

import static java.util.regex.Pattern.compile
import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix
import static pl.allegro.tech.build.axion.release.TagPrefixConf.defaultPrefix
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class GitRepositoryTest extends Specification {

    public static final String MASTER_BRANCH = "master"
    File repositoryDir

    File remoteRepositoryDir

    Grgit rawRepository

    Grgit remoteRawRepository

    GitRepository remoteRepository

    GitRepository repository

    void setup() {
        remoteRepositoryDir = File.createTempDir('axion-release', 'tmp')
        Map remoteRepositories = GitProjectBuilder.gitProject(remoteRepositoryDir).withInitialCommit().build()
        remoteRawRepository = remoteRepositories[Grgit]
        remoteRepository = remoteRepositories[GitRepository]

        repositoryDir = File.createTempDir('axion-release', 'tmp')
        Map repositories = GitProjectBuilder.gitProject(repositoryDir, remoteRepositoryDir).build()

        rawRepository = repositories[Grgit]
        repository = repositories[GitRepository]
    }

    def "should throw unavailable exception when initializing in unexisitng repository"() {
        given:
        File gitlessProject = File.createTempDir('axion-release', 'tmp')
        ScmProperties scmProperties = scmProperties(gitlessProject).build()

        when:
        new GitRepository(scmProperties)

        then:
        thrown(ScmRepositoryUnavailableException)
    }

    def "should not peel lightweight tags"() {
        given:
        File lightweightTagRepositoryDir = File.createTempDir('axion-release', 'tmp')
        Map repositories = GitProjectBuilder.gitProject(lightweightTagRepositoryDir)
            .withInitialCommit()
            .withLightweightTag(fullPrefix() + '1')
            .build()

        GitRepository lightweightTagRepository = repositories[GitRepository] as GitRepository

        when:
        lightweightTagRepository.tag(fullPrefix() + '2')
        TagsOnCommit tags = lightweightTagRepository.latestTags(compile('^' + defaultPrefix() + '.*'))

        then:
        tags.tags == [fullPrefix() + '1', fullPrefix() + '2']
    }

    def "should create new tag on current commit"() {
        when:
        repository.tag(fullPrefix() + '1')

        then:
        rawRepository.tag.list()*.fullName == ['refs/tags/' + fullPrefix() + '1']
    }

    def "should create tag when on HEAD even if it already exists on the same commit"() {
        given:
        repository.tag(fullPrefix() + '1')

        when:
        repository.tag(fullPrefix() + '1')

        then:
        rawRepository.tag.list()*.fullName == ['refs/tags/' + fullPrefix() + '1']
    }

    def "should throw an exception when creating new tag that already exists and it's not on HEAD"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.commit(['*'], "commit after release")

        when:
        repository.tag(fullPrefix() + '1')

        then:
        ScmException e = thrown(ScmException)
        e.getCause() instanceof RefAlreadyExistsException
        rawRepository.tag.list()*.fullName == ['refs/tags/' + fullPrefix() + '1']
    }

    def "should create commit with given message"() {
        when:
        repository.commit(['*'], 'release commit')

        then:
        rawRepository.log(maxCommits: 1)*.fullMessage == ['release commit']
    }

    def "should signal there are uncommitted changes"() {
        when:
        new File(repositoryDir, 'uncommitted').createNewFile()

        then:
        repository.checkUncommittedChanges()
    }

    def "should return last tag in current position in simple case"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.commit(['*'], "commit after release")

        when:
        TagsOnCommit tags = repository.latestTags(Pattern.compile('^' + defaultPrefix() + '.*'))

        then:
        tags.tags == [fullPrefix() + '1']
    }

    def "should return no tags when no commit in repository"() {
        given:
        GitRepository commitlessRepository = GitProjectBuilder.gitProject(File.createTempDir('axion-release', 'tmp')).build()[GitRepository]

        when:
        TagsOnCommit tags = commitlessRepository.latestTags(Pattern.compile('^' + defaultPrefix() + '.*'))

        then:
        tags.tags == []
    }

    def "should indicate that position is on tag when latest commit is tagged"() {
        given:
        repository.tag(fullPrefix() + '1')

        when:
        TagsOnCommit tags = repository.latestTags(Pattern.compile('' + defaultPrefix() + '.*'))

        then:
        tags.tags == [fullPrefix() + '1']
    }

    def "should track back to older tag when commit was made after checking out older version"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.commit(['*'], "commit after " + fullPrefix() + "1")
        repository.tag(fullPrefix() + '2')
        repository.commit(['*'], "commit after " + fullPrefix() +"2")

        rawRepository.checkout(branch: fullPrefix() + '1')
        repository.commit(['*'], "bugfix after " + fullPrefix() + "1")

        when:
        TagsOnCommit tags = repository.latestTags(Pattern.compile("^" + defaultPrefix() + ".*"))

        then:
        tags.tags == [fullPrefix() + '1']
    }

    def "should return all tagged commits matching the pattern provided"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.commit(['*'], "commit after " + fullPrefix() +"1")
        repository.tag(fullPrefix() + '2')
        repository.commit(['*'], "commit after " + fullPrefix() +"2")
        repository.tag('another-tag-1')
        repository.commit(['*'], "commit after another-tag-1")
        repository.commit(['*'], "commit after another-tag-1-2")
        repository.tag(fullPrefix() + '4')
        repository.commit(['*'], "commit after " + fullPrefix() + "4")
        repository.tag(fullPrefix() + '3')
        repository.commit(['*'], "commit after " + fullPrefix() + "3")

        when:
        List<TagsOnCommit> allTaggedCommits = repository.taggedCommits(Pattern.compile('^' + defaultPrefix() + '.*'))

        then:
        allTaggedCommits.collect { c -> c.tags[0] } == [fullPrefix() +'3',fullPrefix() + '4', fullPrefix() + '2', fullPrefix() +'1']
    }

    def "should return only tags that match with prefix"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.commit(['*'], "commit after " + fullPrefix() +"1")
        repository.tag('otherTag')

        when:
        TagsOnCommit tags = repository.latestTags(Pattern.compile('^' + defaultPrefix() + '.*'))

        then:
        tags.tags == [fullPrefix() + '1']
    }

    def "should return latest tagged commit before the given commit id"() {
        given:
        repository.tag('tag-to-find')
        repository.commit(['*'], 'some commit')
        repository.tag('tag-to-skip')

        String latestCommitId = repository.latestTags(~'^tag.*').commitId

        when:
        TagsOnCommit tags = repository.latestTags(~'^tag.*', latestCommitId)

        then:
        tags.tags == ['tag-to-find']
    }

    def "should return list of tags when multiple matching tags found on same commit"() {
        given:
        repository.tag(fullPrefix() + '1')
        repository.tag(fullPrefix() + '2')

        when:
        TagsOnCommit tags = repository.latestTags(Pattern.compile('^' + defaultPrefix() + '.*'))

        then:
        tags.tags == [fullPrefix() + '1', fullPrefix() + '2']
    }

    def "should attach to remote repository"() {
        when:
        repository.attachRemote('testRemote', 'whatever')

        then:
        Config config = rawRepository.repository.jgit.repository.config
        config.getSubsections('remote').contains('testRemote')

        RemoteConfig remote = new RemoteConfig(config, 'testRemote')
        remote.pushURIs == [new URIish('whatever')]
    }

    def "should provide current branch name and commit id in position"() {
        given:
        rawRepository.checkout(branch: 'some-branch', createBranch: true)
        repository.commit(['*'], "first commit")

        when:
        ScmPosition position = repository.currentPosition()

        then:
        println rawRepository.branch.current.name
        position.branch == 'some-branch'
        position.revision == rawRepository.head().id
    }

    def "should provide current branch name as HEAD when in detached state and overriddenBranchName not set"() {
        given:
        String headCommitId = rawRepository.repository.jgit.repository.resolve(Constants.HEAD).name()
        rawRepository.repository.jgit.checkout().setName(headCommitId).call()

        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'HEAD'
    }

    def "should provide current branch name as HEAD when in detached state and overriddenBranchName is empty"() {
        given:
        File repositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(repositoryDir)
            .withOverriddenBranchName("")
            .build()
        Map repositories = GitProjectBuilder.gitProject(repositoryDir, remoteRepositoryDir).usingProperties(scmProperties).build()

        Grgit rawRepository = repositories[Grgit]
        GitRepository repository = repositories[GitRepository]

        String headCommitId = rawRepository.repository.jgit.repository.resolve(Constants.HEAD).name()
        rawRepository.repository.jgit.checkout().setName(headCommitId).call()
        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'HEAD'
    }

    def "should provide current branch name from overriddenBranchName when in detached state and overriddenBranchName is set"() {
        given:
        File repositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(repositoryDir)
            .withOverriddenBranchName("refs/heads/feature/overridden-branch-name")
            .build()
        Map repositories = GitProjectBuilder.gitProject(repositoryDir, remoteRepositoryDir).usingProperties(scmProperties).build()

        Grgit rawRepository = repositories[Grgit]
        GitRepository repository = repositories[GitRepository]

        String headCommitId = rawRepository.repository.jgit.repository.resolve(Constants.HEAD).name()
        rawRepository.repository.jgit.checkout().setName(headCommitId).call()

        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'feature/overridden-branch-name'
    }

    def "should ignore overriddenBranchName when not in detached state"() {
        given:
        File repositoryDir = File.createTempDir('axion-release', 'tmp')
        def scmProperties = scmProperties(repositoryDir)
            .withOverriddenBranchName("refs/heads/feature/overridden-branch-name")
            .build()
        Map repositories = GitProjectBuilder.gitProject(repositoryDir, remoteRepositoryDir).usingProperties(scmProperties).build()

        Grgit rawRepository = repositories[Grgit]
        GitRepository repository = repositories[GitRepository]

        rawRepository.checkout(branch: 'some-branch', createBranch: true)

        when:
        ScmPosition position = repository.currentPosition()

        then:
        position.branch == 'some-branch'
    }

    def "should push changes and tag to remote"() {
        given:
        repository.tag('release-push')
        repository.commit(['*'], 'commit after release-push')

        when:
        repository.push(ScmIdentity.defaultIdentityWithoutAgents(), new ScmPushOptions('origin', false), true)

        then:
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['commit after release-push']
        remoteRawRepository.tag.list()*.fullName == ['refs/tags/release-push']
    }

    def "should not push commits if the pushTagsOnly flag is set to true"() {
        repository.tag('release-push')
        repository.commit(['*'], 'commit after release-push')

        when:
        repository.push(ScmIdentity.defaultIdentityWithoutAgents(), new ScmPushOptions('origin', true))

        then:
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['InitialCommit']
        remoteRawRepository.tag.list()*.fullName == ['refs/tags/release-push']
    }

    def "should push changes to remote with custom name"() {
        given:
        File customRemoteProjectDir = ProjectBuilder.builder().build().file('./')
        Grgit customRemoteRawRepository = Grgit.init(dir: customRemoteProjectDir)

        repository.tag(fullPrefix() + 'custom')
        repository.commit(['*'], 'commit after ' + fullPrefix() + 'custom')
        repository.attachRemote('customRemote', "file://$customRemoteProjectDir.canonicalPath")

        when:
        repository.push(ScmIdentity.defaultIdentityWithoutAgents(), new ScmPushOptions('customRemote', false), true)

        then:
        customRemoteRawRepository.log(maxCommits: 1)*.fullMessage == ['commit after ' + fullPrefix() + 'custom']
        customRemoteRawRepository.tag.list()*.fullName == ['refs/tags/' + fullPrefix() +'custom']
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['InitialCommit']
        remoteRawRepository.tag.list()*.fullName == []
    }

    def "should return error on push failure"() {
        expect: 'this test is implemented as part of testRemote suite in RemoteRejectionTest'
        true
    }

    def "should fetch tags from remote repository"() {
        given:
        remoteRepository.commit(['*'], 'remote commit')
        remoteRepository.tag("remote-tag-to-fetch")

        when:
        repository.fetchTags(ScmIdentity.defaultIdentityWithoutAgents(), 'origin')

        then:
        rawRepository.tag.list().size() == 1
    }

    def "should remove tag"() {
        given:
        repository.tag(fullPrefix() +"1")
        int intermediateSize = repository.taggedCommits(~/.*/).size()

        when:
        repository.dropTag(fullPrefix() +"1")

        then:
        intermediateSize == 1
        repository.taggedCommits(~/.*/).isEmpty()
    }

    def "should pass ahead of remote check when in sync with remote"() {
        expect:
        !repository.checkAheadOfRemote()
    }

    def "should fail ahead of remote check when repository behind remote"() {
        given:
        remoteRepository.commit(["*"], "remote commit")
        repository.fetchTags(ScmIdentity.defaultIdentityWithoutAgents(), "origin")

        expect:
        repository.checkAheadOfRemote()
    }

    def "should fail ahead of remote check when repository has local commits"() {
        given:
        repository.commit(["*"], "local commit")

        expect:
        repository.checkAheadOfRemote()
    }

    def "should fail ahead of remote check when on branch with no remote tracking"() {
        given:
        def repos = GitProjectBuilder.gitProject(File.createTempDir('axion-release', 'tmp'))
            .withInitialCommit()
            .build()

        GitRepository noRemoteRepository = repos[GitRepository]
        Grgit rawRepository = repos[Grgit]

        rawRepository.branch.add(name: 'new-branch')
        rawRepository.branch.change(name: 'new-branch', startPoint: 'new-branch')

        when:
        noRemoteRepository.checkAheadOfRemote()

        then:
        thrown(ScmException)
    }

    def "existing legacy default tagname repo should return true on all matches"() {
        given:
        repository.tag("release-1")
        repository.tag("release-9")

        when:
        boolean isLegacyNamed = repository.isLegacyDefTagnameRepo()

        then:
        isLegacyNamed
    }

        def "existing legacy default tagname repo should return false on partially matches"() {
        given:
        repository.tag("release-1")
        repository.tag("bla1")

        when:
        boolean isLegacyNamed = repository.isLegacyDefTagnameRepo()

        then:
        !isLegacyNamed
    }

    def "existing legacy default tagname repo should return false on other matches"() {
        given:
        repository.tag("bla1")

        when:
        boolean isLegacyNamed = repository.isLegacyDefTagnameRepo()

        then:
        !isLegacyNamed
    }

    def "existing legacy default tagname repo should return false on empty matches"() {

        when:
        boolean isLegacyNamed = repository.isLegacyDefTagnameRepo()

        then:
        !isLegacyNamed
    }

    def "existing legacy default tagname repo should return false on current default tag matches"() {

        given:
        repository.tag(fullPrefix() + "1")

        when:
        boolean isLegacyNamed = repository.isLegacyDefTagnameRepo()

        then:
        !isLegacyNamed
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
        ScmPosition position = repository.positionOfLastChangeIn('a\\aa', [])

        then:
        position.revision == headSubDirAChanged
    }

    def "last position with monorepo paths case: merge after squash"() {
        given:
        Git git = repository.getJgitRepository();
        commitFile('b4/aa', 'b4')
        commitFile('b4/ab', 'b4b')

        String importantDir = 'a/aa'

        commitFile(importantDir, 'foo')
        String headSubDirAChanged = rawRepository.head().id

        String secondBranchName = "feature/unintresting_changes";
        git.branchCreate().setName(secondBranchName).call()
        git.checkout().setName(secondBranchName).call()
        commitFile('second/aa', 'foo')
        commitFile('b/ba', 'bar')
        git.checkout().setName(MASTER_BRANCH).call()
        git.merge().include(git.repository.resolve(secondBranchName)).setCommit(true).setMessage("unintresting").setFastForward(MergeCommand.FastForwardMode.NO_FF).call()

        commitFile('after/aa', 'after')

        when:
        ScmPosition position = repository.positionOfLastChangeIn(importantDir, [])

        then:
        position.revision == headSubDirAChanged
    }

    private void commitFile(String subDir, String fileName) {
        String fileInA = "${subDir}/${fileName}"
        new File(repositoryDir, subDir).mkdirs()
        new File(repositoryDir, fileInA).createNewFile()
        repository.commit([fileInA], "Add file ${fileName} in ${subDir}")
    }

}
