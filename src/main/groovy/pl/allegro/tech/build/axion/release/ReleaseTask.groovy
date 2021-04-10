package pl.allegro.tech.build.axion.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext

class ReleaseTask extends DefaultTask {

    @Input
    @Optional
    VersionConfig versionConfig

    @TaskAction
    void release() {
        VersionConfig config = GradleAwareContext.configOrCreateFromProject(project, versionConfig)
        Context context = GradleAwareContext.create(project, config)
        Releaser releaser = context.releaser()
        ScmPushResult result = releaser.releaseAndPush(context.rules())

        if(!result.success) {
            def message = result.remoteMessage.orElse("Unknown error during push")
            logger.error("remote message: ${message}")
            throw new ReleaseFailedException(message)
        }
    }

    void setVersionConfig(VersionConfig versionConfig) {
        this.versionConfig = versionConfig
    }
}
