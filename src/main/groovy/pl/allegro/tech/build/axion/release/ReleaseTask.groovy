package pl.allegro.tech.build.axion.release

import com.github.zafarkhaja.semver.Version
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.ComponentFactory
import pl.allegro.tech.build.axion.release.infrastructure.dry.DryRepository

class ReleaseTask extends DefaultTask {

    static final String DRY_RUN_FLAG = 'release.dryRun'

    private final VersionConfig versionConfig

    private final Releaser releaser

    ReleaseTask() {
        this.versionConfig = project.extensions.getByType(VersionConfig)
        this.releaser = new Releaser(
                createRepository(project, versionConfig),
                new LocalOnlyResolver(versionConfig, project),
                logger
        )
    }

    private ScmRepository createRepository(Project project, VersionConfig versionConfig) {
        ScmRepository scm = ComponentFactory.scmRepository(project, versionConfig)
        return project.hasProperty(DRY_RUN_FLAG)? new DryRepository(scm, project.logger) : scm
    }

    @TaskAction
    void release() {
        releaser.release(versionConfig)
    }
}
