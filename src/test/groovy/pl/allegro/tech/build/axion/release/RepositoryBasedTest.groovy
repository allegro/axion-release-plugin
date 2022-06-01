package pl.allegro.tech.build.axion.release

import org.ajoberstar.grgit.Grgit
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory
import spock.lang.Specification
import spock.lang.TempDir

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class RepositoryBasedTest extends Specification {

    @TempDir
    File temporaryFolder

    VersionResolutionContext context

    ScmRepository repository

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
    }

    protected String currentVersion() {
        return context.versionService().currentDecoratedVersion(
                context.rules().version,
                context.rules().tag,
                context.rules().nextVersion
        ).decoratedVersion
    }
}
