package pl.allegro.tech.build.axion.release.infrastructure.di

import org.ajoberstar.grgit.Grgit
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class ScmRepositoryFactoryTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    ScmRepositoryFactory factory = new ScmRepositoryFactory()

    def "should return git repository by default"() {
        given:
        Grgit.init(dir: temporaryFolder.root)
        ScmProperties properties = scmProperties(temporaryFolder.root).build()

        expect:
        factory.create(properties) instanceof GitRepository
    }

    def "should return dummy repository when underlying repository is not initialized"() {
        given:
        ScmProperties properties = scmProperties(temporaryFolder.root).build()

        expect:
        factory.create(properties) instanceof DummyRepository
    }

    def "should throw exception when trying to construct unknown type of repository"() {
        given:
        ScmProperties properties = scmProperties(temporaryFolder.root).ofType('unknown').build()

        when:
        factory.create(properties)

        then:
        thrown(IllegalArgumentException)
    }
}
