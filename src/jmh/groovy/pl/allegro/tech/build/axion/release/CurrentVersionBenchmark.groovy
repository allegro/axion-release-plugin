package pl.allegro.tech.build.axion.release

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.grgit.Grgit
import org.openjdk.jmh.annotations.*
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory

@State(value = Scope.Benchmark)
class CurrentVersionBenchmark {

    private static final COMMITS = 1_000

    private static final TAG_COMMIT = 800

    private final File repositoryDir = File.createTempDir('axion-release-perf', null)

    @Setup(Level.Trial)
    void prepare() {
        Grgit.init(dir: repositoryDir)

        ScmProperties scmProperties = ScmPropertiesBuilder.scmProperties(repositoryDir).build()
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        Context context = new Context(
            PropertiesBuilder.properties().build(),
            scmRepository,
            scmProperties,
            new LocalOnlyResolver(true)
        )

        ScmRepository repository = context.repository()
        repository.commit(['*'], 'initial commit')

        File dir = repositoryDir
        for (int i = 0; i < COMMITS; ++i) {
            if (i == TAG_COMMIT) {
                repository.tag('release-1.0.0')
            }
            repository.commit(['*'], "commit #$i")
        }
    }

    @Benchmark
    Version currentVersion() {
        ScmProperties scmProperties = ScmPropertiesBuilder.scmProperties(repositoryDir).build()
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        Context context = new Context(
            PropertiesBuilder.properties().build(),
            scmRepository,
            scmProperties,
            new LocalOnlyResolver(true)
        )

        return context.versionService().currentVersion(
            VersionPropertiesBuilder.versionProperties().build(),
            TagPropertiesBuilder.tagProperties().build(),
            NextVersionPropertiesBuilder.nextVersionProperties().build()
        ).version
    }

}
