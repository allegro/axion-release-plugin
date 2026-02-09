package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.TagMessageConf
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

enum TagMessageCreator {
    DEFAULT('default',
        { ScmPosition position, String version, String previousVersion ->
            return TagMessageConf.defaultMessage()
        }
    )

    private final String type

    final TagProperties.MessageCreator messageCreator

    private TagMessageCreator(String type, TagProperties.MessageCreator messageCreator) {
        this.type = type
        this.messageCreator = messageCreator
    }
}
