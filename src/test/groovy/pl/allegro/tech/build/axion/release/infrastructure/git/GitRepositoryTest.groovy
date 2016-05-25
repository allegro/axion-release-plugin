package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class GitRepositoryTest extends Specification {

    Project project

    Grgit rawRepository

    Grgit remoteRawRepository

    GitRepository repository

    void setup() {
        Project remoteProject = ProjectBuilder.builder().build()
        remoteRawRepository = GitProjectBuilder.gitProject(remoteProject).withInitialCommit().build()[Grgit]

        project = ProjectBuilder.builder().build()
        Map repositories = GitProjectBuilder.gitProject(project, remoteProject).build()

        rawRepository = repositories[Grgit]
        repository = repositories[GitRepository]
    }

    def "should throw unavailable exception when initializing in unexisitng repository"() {
        given:
        Project gitlessProject = ProjectBuilder.builder().build()
        ScmProperties scmProperties = scmProperties(gitlessProject.rootDir).build()

        when:
        new GitRepository(scmProperties)

        then:
        thrown(ScmRepositoryUnavailableException)
    }

    def "should create new tag on current commit"() {
        when:
        repository.tag('release-1')

        then:
        rawRepository.tag.list()*.fullName == ['refs/tags/release-1']
    }

    def "should create new tag if it exists and it's on HEAD"() {
        given:
        repository.tag('release-1')

        when:
        repository.tag('release-1')

        then:
        rawRepository.tag.list()*.fullName == ['refs/tags/release-1']
    }

    def "should throw an exception if create new tag if it exists before HEAD"() {
        given:
        repository.tag('release-1')
        repository.commit(['*'], "commit after release")

        when:
        repository.tag('release-1')

        then:
        thrown(GrgitException)
        rawRepository.tag.list()*.fullName == ['refs/tags/release-1']
    }

    def "should create commit with given message"() {
        when:
        repository.commit(['*'], 'release commit')

        then:
        rawRepository.log(maxCommits: 1)*.fullMessage == ['release commit']
    }

    def "should signal there are uncommitted changes"() {
        when:
        project.file('uncommitted').createNewFile()

        then:
        repository.checkUncommittedChanges()
    }

    def "should point to last tag in current position in simple case"() {
        given:
        repository.tag('release-1')
        repository.commit(['*'], "commit after release")

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.latestTag == 'release-1'
        !position.onTag
    }

    def "should use the given closure to select one of multiple tags on the same commit"() {
        given:
        repository.tag('release-1.0.0-rc1')
        repository.commit(['*'], "commit after release")
        repository.tag('release-1.0.0-rc2')
        repository.tag('release-1.0.0')

        Closure<String> tagSelector = { tags ->
            assert tags == ['release-1.0.0', 'release-1.0.0-rc2']
            return 'release-1.0.0'
        }

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/, tagSelector)

        then:
        position.latestTag == 'release-1.0.0'
        position.onTag
    }

    def "should return default position when no commit in repository"() {
        given:
        GitRepository commitlessRepository = GitProjectBuilder.gitProject(ProjectBuilder.builder().build()).build()[GitRepository]

        when:
        ScmPosition position = commitlessRepository.currentPosition(~/^release.*/)

        then:
        position.branch == 'master'
        position.tagless()
    }

    def "should indicate that position is on tag when latest commit is tagged"() {
        given:
        repository.tag('release-1')

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.latestTag == 'release-1'
        position.onTag
    }

    def "should track back to older tag when commit was made after checking out older version"() {
        given:
        repository.tag('release-1')
        repository.commit(['*'], "commit after release-1")
        repository.tag('release-2')
        repository.commit(['*'], "commit after release-2")

        rawRepository.checkout(branch: 'release-1')
        repository.commit(['*'], "bugfix after release-1")

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.latestTag == 'release-1'
    }

    def "should return tagless position with branch name when no tag in repository"() {
        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.branch == 'master'
        position.tagless()
        !position.onTag
    }

    def "should return only tags that match with prefix"() {
        given:
        repository.tag('release-1')
        repository.commit(['*'], "commit after release-1")
        repository.tag('otherTag')

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.latestTag == 'release-1'
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

    def "should provide current branch name in position"() {
        given:
        repository.checkoutBranch('some-branch')
        repository.commit(['*'], "first commit")

        when:
        ScmPosition position = repository.currentPosition(~/^release.*/)

        then:
        position.branch == 'some-branch'
    }

    def "should push changes and tag to remote"() {
        given:
        repository.tag('release-push')
        repository.commit(['*'], 'commit after release-push')

        when:
        repository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: false), true)

        then:
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['commit after release-push']
        remoteRawRepository.tag.list()*.fullName == ['refs/tags/release-push']
    }

    def "should not push commits if the pushTagsOnly flag is set to true"() {
        repository.tag('release-push')
        repository.commit(['*'], 'commit after release-push')

        when:
        repository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'origin', pushTagsOnly: true))

        then:
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['InitialCommit']
        remoteRawRepository.tag.list()*.fullName == ['refs/tags/release-push']
    }

    def "should push changes to remote with custom name"() {
        given:
        File customRemoteProjectDir = ProjectBuilder.builder().build().file('./')
        Grgit customRemoteRawRepository = Grgit.init(dir: customRemoteProjectDir)

        repository.tag('release-custom')
        repository.commit(['*'], 'commit after release-custom')
        repository.attachRemote('customRemote', "file://$customRemoteProjectDir.canonicalPath")

        when:
        repository.push(ScmIdentity.defaultIdentity(), new ScmPushOptions(remote: 'customRemote', pushTagsOnly: false), true)

        then:
        customRemoteRawRepository.log(maxCommits: 1)*.fullMessage == ['commit after release-custom']
        customRemoteRawRepository.tag.list()*.fullName == ['refs/tags/release-custom']
        remoteRawRepository.log(maxCommits: 1)*.fullMessage == ['InitialCommit']
        remoteRawRepository.tag.list()*.fullName == []
    }
}
