package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.TagNameSerializer

class TagPropertiesBuilder {

    private String prefix = TagPrefixConf.defaultPrefix()

    private TagPropertiesBuilder() {
    }

    static TagPropertiesBuilder tagProperties() {
        return new TagPropertiesBuilder()
    }

    TagProperties build() {
        return new TagProperties(
            prefix,
            TagPrefixConf.defaultSeparator(),
            TagNameSerializer.DEFAULT.serializer,
            TagNameSerializer.DEFAULT.deserializer,
            { r, p -> '0.1.0' }
        )
    }

    TagPropertiesBuilder withPrefix(String prefix) {
        this.prefix = prefix
        return this
    }

}
