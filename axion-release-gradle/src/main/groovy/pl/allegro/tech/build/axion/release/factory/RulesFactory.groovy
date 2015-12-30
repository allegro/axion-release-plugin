package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties

class RulesFactory {

    static Properties create(Project project, VersionConfig versionConfig, String currentBranch) {
        return new Properties(
                project.hasProperty(ReleasePlugin.DRY_RUN_FLAG),
                VersionPropertiesFactory.create(project, versionConfig, currentBranch),
                TagPropertiesFactory.create(versionConfig.tag, currentBranch),
                ChecksPropertiesFactory.create(project, versionConfig.checks),
                NextVersionPropertiesFactory.create(project, versionConfig.nextVersion),
                HooksPropertiesFactory.create(versionConfig, versionConfig.hooks)
        )
    }

}
