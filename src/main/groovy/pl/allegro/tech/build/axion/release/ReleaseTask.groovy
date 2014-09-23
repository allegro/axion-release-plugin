package pl.allegro.tech.build.axion.release

import com.github.zafarkhaja.semver.Version
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.ComponentFactory
import pl.allegro.tech.build.axion.release.infrastructure.dry.DryRepository

class ReleaseTask extends DefaultTask {

    static final String DRY_RUN_FLAG = 'release.dryRun'

    private final VersionConfig versionConfig

    private final ScmRepository repository

    public ReleaseTask() {
        this.versionConfig = project.extensions.getByType(VersionConfig)
        this.repository = createRepository(project, versionConfig)
    }

    private ScmRepository createRepository(Project project, VersionConfig versionConfig) {
        ScmRepository scm = ComponentFactory.scmRepository(project, versionConfig)
        return project.hasProperty(DRY_RUN_FLAG)? new DryRepository(scm, project.logger) : scm
    }

    @TaskAction
    void release() {
        VersionWithPosition positionedVersion = versionConfig.getRawVersion()
        Version version = positionedVersion.version

        if (version.preReleaseVersion == VersionService.SNAPSHOT) {
            version = version.setPreReleaseVersion(null)
            String tagName = versionConfig.tag.serialize(versionConfig.tag, version.toString())

            project.logger.quiet("Creating tag: $tagName")
            repository.tag(tagName)
            project.logger.quiet("Pushing all to remote: ${versionConfig.remote}")
            repository.push(versionConfig.remote)
        }
        else {
            project.logger.quiet("Working on released version ${versionConfig.version}, nothing to do here.")
        }
    }
}
