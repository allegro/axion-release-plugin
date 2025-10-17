package pl.allegro.tech.build.axion.release

import org.ajoberstar.grgit.Grgit
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext
import spock.lang.Specification
import spock.lang.TempDir

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class RepositoryBasedTest extends Specification {

    @TempDir
    File temporaryFolder

    VersionResolutionContext context

    ScmRepository repository

    String defaultBranch

    private Grgit rawRepository

    void setup() {
        rawRepository = Grgit.init(dir: temporaryFolder)
        defaultBranch = rawRepository.branch.current().name

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
        repository.commit(['.'], 'initial commit')
    }

    void checkout(String branch) {
        rawRepository.checkout([branch: branch, createBranch: true])
    }

    void createTag(String tag) {
        rawRepository.tag.add(name: tag)
    }

    protected String currentVersion() {
        return context.versionService().currentDecoratedVersion(
            context.rules().version,
            context.rules().tag,
            context.rules().nextVersion
        ).decoratedVersion
    }
}
