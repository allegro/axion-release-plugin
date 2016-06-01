package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.TagNameSerializer
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;

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
                initialVersion: { r, p -> '0.1.0' },
                tagSelector: ScmRepository.LAST_TAG_SELECTOR
        )
    }

}
