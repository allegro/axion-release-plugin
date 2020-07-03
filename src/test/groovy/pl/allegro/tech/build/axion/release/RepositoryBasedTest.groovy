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

    static void setupABranchWithHighTagAndBBranchWithLowTag(ScmRepository repository) {
        // * 49f4094 (tag: release-2.0.0, high) some commit 2
        // | * 662c593 (HEAD -> low, tag: release-1.0.1) some commit 3
        // |/
        // * 4b76059 (tag: release-1.0.0, start) some commit  1
        // * b21eb90 (master) initial commit
        repository.branch('start')
        repository.checkout('start')
        repository.commit(['*'], 'some commit  1')
        repository.tag('release-1.0.0')
        repository.branch('high')
        repository.checkout('high')
        repository.commit(['*'], 'some commit 2')
        repository.tag('release-2.0.0')
        repository.checkout('start')
        repository.branch('low')
        repository.checkout('low')
        repository.commit(['*'], 'some commit 3')
        repository.tag('release-1.0.1')
    }

    void setup() {
        directory = temporaryFolder.root
        Grgit.init(dir: directory)

        ScmProperties scmProperties = scmProperties(directory).build()
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        context = new Context(
                PropertiesBuilder.properties().build(),
                scmRepository,
                scmProperties,
                directory,
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
