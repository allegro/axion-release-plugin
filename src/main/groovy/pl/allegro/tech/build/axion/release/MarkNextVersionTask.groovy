package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext

class MarkNextVersionTask extends DefaultTask {

    @Optional
    VersionConfig versionConfig

    @TaskAction
    void release() {
        VersionConfig config = GradleAwareContext.configOrCreateFromProject(project, versionConfig)
        Context context = GradleAwareContext.create(project, config)

        Properties rules = context.rules()
        NextVersionMarker marker = new NextVersionMarker(context.scmService(), rules.getVersion().getMonorepoProperties().getDirsToExclude())

        marker.markNextVersion(context.projectRootRelativePath(), rules.nextVersion, rules.tag, config)
    }

}
