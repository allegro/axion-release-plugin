package pl.allegro.tech.build.axion.release

import org.ajoberstar.grgit.Grgit
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.properties.PropertiesBuilder
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class RepositoryBasedTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File directory

    Context context

    ScmRepository repository

    void setup() {
        directory = temporaryFolder.root
        Grgit.init(dir: directory)

        ScmProperties scmProperties = scmProperties(directory).build()
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        context = new Context(
                PropertiesBuilder.properties().build(),
                scmRepository,
                scmProperties,
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
