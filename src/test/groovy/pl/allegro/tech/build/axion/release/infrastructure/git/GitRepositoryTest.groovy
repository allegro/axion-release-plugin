package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.lib.Config
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class GitRepositoryTest extends Specification {

    Project project

    Grgit rawRepository

    GitRepository repository

    void setup() {
        project = ProjectBuilder.builder().build()
        File projectDir = project.file('./')

        rawRepository = Grgit.init(dir: projectDir)
        rawRepository.add(patterns: ['*'])
        rawRepository.commit(message: 'InitialCommit')

        repository = new GitRepository(projectDir, null)
    }

    def "should create new tag on current commit"() {
        when:
        repository.tag('release-1')

        then:
        rawRepository.tag.list()*.fullName == ['refs/tags/release-1']
    }

    def "should create commit with given message"() {
        when:
        repository.commit('release commit')

        then:
        rawRepository.log(maxCommits: 1)*.fullMessage == ['release commit']
    }

    def "should signal there are uncommited changes"() {
        when:
        project.file('uncommited').createNewFile()

        then:
        repository.checkUncommitedChanges() == true
    }

    def "should point to last tag in current position in simple case"() {
        given:
        repository.tag('release-1')
        repository.commit("commit after release")

        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.latestTag == 'release-1'
        position.onTag == false
    }

    def "should indicate that position is on tag when latest commit is tagged"() {
        given:
        repository.tag('release-1')

        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.latestTag == 'release-1'
        position.onTag == true
    }

    def "should track back to older tag when commit was made after checking out older version"() {
        given:
        repository.tag('release-1')
        repository.commit("commit after release-1")
        repository.tag('release-2')
        repository.commit("commit after release-2")

        rawRepository.checkout(branch: 'release-1')
        repository.commit("bugfix after release-1")

        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.latestTag == 'release-1'
    }

    def "should return tagless position with branch name when no tag in repository"() {
        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.branch == 'master'
        position.tagless()
        !position.onTag
    }

    def "should return only tags that match with prefix"() {
        given:
        repository.tag('release-1')
        repository.commit("commit after release-1")
        repository.tag('otherTag')

        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.latestTag == 'release-1'
    }

    def "should attach to remote repository"() {
        when:
        repository.attachRemote('testRemote', 'whatever')

        then:
        Config config = rawRepository.repository.jgit.repository.config
        config.getSubsections('remote').contains('testRemote')
        config.getString('remote', 'testRemote', 'url') == 'whatever'
    }

    def "should provide current branch name in position"() {
        given:
        repository.checkoutBranch('some-branch')
        repository.commit("first commit")

        when:
        ScmPosition position = repository.currentPosition('release')

        then:
        position.branch == 'some-branch'
    }
}
