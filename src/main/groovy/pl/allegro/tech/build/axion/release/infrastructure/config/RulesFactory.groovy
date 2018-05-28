package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class RulesFactory {

    static Properties create(Project project, VersionConfig versionConfig, ScmPosition position) {
        return new Properties(
            project.hasProperty(ReleasePlugin.DRY_RUN_FLAG),
            VersionPropertiesFactory.create(project, versionConfig, position.branch),
            TagPropertiesFactory.create(versionConfig.tag, position.branch),
            ChecksPropertiesFactory.create(project, versionConfig.checks),
            NextVersionPropertiesFactory.create(project, versionConfig),
            HooksPropertiesFactory.create(versionConfig, versionConfig.hooks)
        )
    }

}
