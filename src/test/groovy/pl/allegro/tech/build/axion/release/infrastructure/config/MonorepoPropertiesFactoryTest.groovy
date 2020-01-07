package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig
import pl.allegro.tech.build.axion.release.domain.properties.MonorepoProperties
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class MonorepoPropertiesFactoryTest extends Specification {

    private Project project

    private MonorepoConfig monorepoConfig

    def setup() {
        project = builder().build()
        monorepoConfig = new MonorepoConfig()
    }

    def "should copy properties from MonorepoConfig object"() {
        given:
        monorepoConfig.projectDirs = ["foo", "bar"]

        when:
        MonorepoProperties rules = MonorepoPropertiesFactory.create(project, monorepoConfig, 'master')

        then:
        rules.dirsToExclude.size() == 2
        rules.dirsToExclude.contains("foo")
        rules.dirsToExclude.contains("bar")
    }
}
