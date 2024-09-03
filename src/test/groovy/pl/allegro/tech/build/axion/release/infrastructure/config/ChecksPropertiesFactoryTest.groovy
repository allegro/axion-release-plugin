package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.internal.provider.DefaultProviderFactory
import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.ChecksConfig
import spock.lang.Specification

class ChecksPropertiesFactoryTest extends Specification {

    def "should return default value from config when no project properties are present"() {
        given:
        ChecksConfig config = Fixtures.checksConfig(Fixtures.project())
        config.aheadOfRemote.set(true)
        config.uncommittedChanges.set(true)
        config.snapshotDependencies.set(true)

        expect:
        config.checkUncommittedChanges().get()
        config.checkAheadOfRemote().get()
        config.checkSnapshotDependencies().get()
    }

    def "should always return false if checks are globally disabled using release.disableChecks"() {
        given:
        ChecksConfig config = Spy(Fixtures.checksConfig()) {
            gradlePropertyPresent("release.disableChecks") >> new DefaultProviderFactory().provider({ true })
        }

        config.aheadOfRemote.set(true)
        config.uncommittedChanges.set(true)
        config.snapshotDependencies.set(true)

        expect:
        !config.checkUncommittedChanges().get()
        !config.checkAheadOfRemote().get()
        !config.checkSnapshotDependencies().get()
    }

    def "should skip uncommitted changes check if it was disabled using project property"() {
        given:
        ChecksConfig config = Spy(Fixtures.checksConfig()) {
            gradlePropertyPresent("release.disableUncommittedCheck") >> new DefaultProviderFactory().provider({ true })
        }

        config.uncommittedChanges.set(true)

        expect:
        !config.checkUncommittedChanges().get()
    }

    def "should skip ahead of remote check if it was disabled using project property"() {
        given:
        ChecksConfig config = Spy(Fixtures.checksConfig()) {
            gradlePropertyPresent('release.disableRemoteCheck') >> new DefaultProviderFactory().provider({ true })
        }
        config.aheadOfRemote.set(true)

        expect:
        !config.checkAheadOfRemote().get()
    }

    def "should skip snapshots check if it was disabled using project property"() {
        given:
        ChecksConfig config = Spy(Fixtures.checksConfig()) {
            gradlePropertyPresent('release.disableSnapshotsCheck') >> new DefaultProviderFactory().provider({ true })
        }

        config.snapshotDependencies.set(true)

        expect:
        !config.checkSnapshotDependencies().get()
    }

}
