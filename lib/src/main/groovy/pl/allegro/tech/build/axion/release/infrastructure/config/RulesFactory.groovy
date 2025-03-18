package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class RulesFactory {

    static Properties create(VersionConfig versionConfig, ScmRepository repository) {

        ScmPosition position = repository.currentPosition()
        setDefaultPrefix(versionConfig.tag, repository)

        return new Properties(
            versionConfig.dryRun.get(),
            VersionPropertiesFactory.create(versionConfig, position.branch),
            TagPropertiesFactory.create(versionConfig.tag, position.branch),
            versionConfig.checks,
            versionConfig.nextVersion.nextVersionProperties(),
            HooksPropertiesFactory.create(versionConfig, versionConfig.hooks)
        )
    }

    static setDefaultPrefix(TagNameSerializationConfig tag, ScmRepository repository) {
        //no conf set, repo has history of old default
        if (tag.getPrefix() == null && tag.getVersionSeparator() == null && repository.isLegacyDefTagnameRepo()) {
            tag.setPrefix(TagPrefixConf.DEFAULT_LEGACY_PREFIX)
            tag.setVersionSeparator(TagPrefixConf.DEFAULT_LEGACY_SEP)
        } else {
            if (tag.getPrefix() == null) {
                tag.setPrefix(TagPrefixConf.defaultPrefix())
            }
            if (tag.getVersionSeparator() == null) {
                tag.setVersionSeparator(TagPrefixConf.defaultSeparator())
            }
        }
    }
}
