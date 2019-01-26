package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.TagNameSerializer

class TagPropertiesBuilder {

    private String prefix = 'release'

    private TagPropertiesBuilder() {
    }

    static TagPropertiesBuilder tagProperties() {
        return new TagPropertiesBuilder()
    }

    TagProperties build() {
        return new TagProperties(
            prefix,
            '-',
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
