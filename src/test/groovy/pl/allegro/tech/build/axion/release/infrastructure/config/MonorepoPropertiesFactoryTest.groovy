package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig
import spock.lang.Specification

class MonorepoPropertiesFactoryTest extends Specification {

    private MonorepoConfig monorepoConfig

    def setup() {
        monorepoConfig = Fixtures.monorepoConfig()
    }

    def "should copy properties from MonorepoConfig object"() {
        when:
        monorepoConfig.projectDirs = ["foo", "bar"]

        then:
        def dirsToExclude = monorepoConfig.projectDirs.get()
        dirsToExclude.size() == 2
        dirsToExclude.contains("foo")
        dirsToExclude.contains("bar")
    }
}
