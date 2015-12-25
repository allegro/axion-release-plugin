package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.TagNameSerializer

class TagPropertiesBuilder {

    private TagPropertiesBuilder() {
    }

    static TagPropertiesBuilder tagProperties() {
        return new TagPropertiesBuilder()
    }

    TagProperties build() {
        return new TagProperties(
                serialize: TagNameSerializer.DEFAULT.serializer,
                deserialize: TagNameSerializer.DEFAULT.deserializer,
                prefix: 'release',
                versionSeparator: '-',
                initialVersion: { r, p -> '0.1.0' }
        )
    }

}
