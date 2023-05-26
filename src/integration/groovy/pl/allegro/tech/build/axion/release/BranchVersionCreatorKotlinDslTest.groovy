package pl.allegro.tech.build.axion.release

import org.ajoberstar.grgit.Grgit
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext
import spock.lang.Specification
import spock.lang.TempDir

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class BranchVersionCreatorKotlinDslTest extends Specification {

    @TempDir
    File temporaryFolder

    ScmRepository repository

    VersionResolutionContext context

    void setup() {
        def rawRepository = Grgit.init(dir: temporaryFolder)

        // let's make sure, not to use system wide user settings in tests
        rawRepository.repository.jgit.repository.config.baseConfig.clear()

        ScmProperties scmProperties = scmProperties(temporaryFolder).build()
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        context = new VersionResolutionContext(
            PropertiesBuilder.properties().build(),
            scmRepository,
            scmProperties,
            temporaryFolder,
            new LocalOnlyResolver(true)
        )

        repository = context.repository()
        repository.commit(['*'], 'initial commit')
        repository.tag("V1.0.0")

    }


    def "should not fail using closure as argument for branchVersionIncrementer"() {
        given:

        new FileTreeBuilder(temporaryFolder).file("build.gradle.kts",
            """

        import com.github.zafarkhaja.semver.Version
        import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext
        import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
        import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

        plugins {
            id ("pl.allegro.tech.build.axion-release")
        }

        scmVersion {
             tag {
                prefix.set("V")
             }
             ignoreUncommittedChanges.set(false)

             branchVersionCreator.putAll(
                mapOf(
                    "master" to VersionProperties.Creator { s: String, scmPosition: ScmPosition ->  "\${s}-\${scmPosition.branch}"}
                )
            )
        }


        project.version = scmVersion.version

        """)

        when:
        def result = GradleRunner.create()
            .withProjectDir(temporaryFolder)
            .withPluginClasspath()
            .withArguments('currentVersion', '-s')
            .build()

        then:
        result.output.contains(" 1.0.1-master-SNAPSHOT")
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS


    }
}
