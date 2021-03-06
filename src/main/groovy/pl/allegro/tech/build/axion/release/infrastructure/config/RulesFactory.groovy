package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class RulesFactory {

    static Properties create(Project project, VersionConfig versionConfig, ScmPosition position) {

        setDefaultPrefix(versionConfig.tag)


        return new Properties(
            project.hasProperty(ReleasePlugin.DRY_RUN_FLAG),
            VersionPropertiesFactory.create(project, versionConfig, position.branch),
            TagPropertiesFactory.create(versionConfig.tag, position.branch),
            ChecksPropertiesFactory.create(project, versionConfig.checks),
            NextVersionPropertiesFactory.create(project, versionConfig.nextVersion),
            HooksPropertiesFactory.create(versionConfig, versionConfig.hooks)
        )
    }

    static setDefaultPrefix(TagNameSerializationConfig tag){
       if(tag.getPrefix() == null) {

           tag.setPrefix(TagPrefixConf.prefix())
       }
        if(tag.getVersionSeparator() == null)
        {

            tag.setVersionSeparator(TagPrefixConf.separator())

        }

    }

}
