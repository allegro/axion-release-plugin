package pl.allegro.tech.build.axion.release.infrastructure.git

import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit
import spock.lang.Specification

class CachedGitRepositoryTest extends Specification {

    File repositoryDir

    Repository jgitRepository

    Grgit rawRepository

    GitRepository repository

    void setup() {
        repositoryDir = File.createTempDir('axion-release', 'tmp')
        Map repositories = GitProjectBuilder.gitProject(repositoryDir).build()

        rawRepository = repositories[Grgit]
        repository = repositories[GitRepository]
        jgitRepository = rawRepository.repository.jgit.repository
    }

    def "should walk till all commits from repo are read"() {
        given:
        repository.commit(['*'], 'commit 1')
        repository.tag('release-1.0.0')
        repository.commit(['*'], 'commit 2')
        repository.commit(['*'], 'commit 3')

        CachedGitRepository cachedGitRepository = new CachedGitRepository(jgitRepository)
        CachedGitRepository.CachedGitRevWalk walk = cachedGitRepository.walk(jgitRepository, head())

        when:
        TagsOnCommit tags = walk.next()

        then:
        tags.getTags() == ['release-1.0.0']
    }

    def "should walk through all tags"() {
        given:
        repository.commit(['*'], 'commit 1')
        repository.tag('release-1.0.0')
        repository.commit(['*'], 'commit 2')
        repository.tag('release-2.0.0')
        repository.commit(['*'], 'commit 3')

        CachedGitRepository cachedGitRepository = new CachedGitRepository(jgitRepository)
        CachedGitRepository.CachedGitRevWalk walk = cachedGitRepository.walk(jgitRepository, head())

        when:
        TagsOnCommit tags = walk.next()

        then:
        tags.tags == ['release-2.0.0']

        when:
        tags = walk.next()

        then:
        tags.tags == ['release-1.0.0']
    }

    def "should be able to perform multiple walks, second starting from other point"() {
        given:
        repository.commit(['*'], 'commit 1')
        repository.tag('release-1.0.0')
        repository.commit(['*'], 'commit 2')
        repository.tag('release-2.0.0')
        repository.commit(['*'], 'commit 3')

        CachedGitRepository cachedGitRepository = new CachedGitRepository(jgitRepository)

        when:
        CachedGitRepository.CachedGitRevWalk walk1 = cachedGitRepository.walk(jgitRepository, head())
        TagsOnCommit tags = walk1.next()

        then:
        tags.tags == ['release-2.0.0']

        when:
        CachedGitRepository.CachedGitRevWalk walk2 = cachedGitRepository.walk(jgitRepository, ObjectId.fromString(tags.commitId))
        tags = walk2.next()

        then:
        tags.tags == ['release-2.0.0']
    }

    def "should invalidate cache when new tag appears"() {
        given:
        repository.commit(['*'], 'commit 1')

        CachedGitRepository cachedGitRepository = new CachedGitRepository(jgitRepository)

        when:
        CachedGitRepository.CachedGitRevWalk walk1 = cachedGitRepository.walk(jgitRepository, head())
        TagsOnCommit tags = walk1.next()

        then:
        tags == null

        when:
        repository.tag('release-1.0.0')
        cachedGitRepository.invalidate(jgitRepository)

        CachedGitRepository.CachedGitRevWalk walk2 = cachedGitRepository.walk(jgitRepository, head())
        tags = walk2.next()

        then:
        tags.tags == ['release-1.0.0']
    }

    private ObjectId head() {
        return ObjectId.fromString(rawRepository.head().id)
    }
}
