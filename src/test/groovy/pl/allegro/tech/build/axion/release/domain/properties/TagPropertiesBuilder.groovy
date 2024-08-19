package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.TagNameSerializer

class TagPropertiesBuilder {

    private String prefix = TagPrefixConf.defaultPrefix()
    private List<String> fallbackPrefixes = Collections.emptyList()
    private String separator = TagPrefixConf.defaultSeparator()

    private TagPropertiesBuilder() {
    }

    static TagPropertiesBuilder tagProperties() {
        return new TagPropertiesBuilder()
    }

    TagProperties build() {
        return new TagProperties(
            prefix,
            fallbackPrefixes,
            separator,
            TagNameSerializer.DEFAULT.serializer,
            TagNameSerializer.DEFAULT.deserializer,
            { r, p -> '0.1.0' }
        )
    }

    TagPropertiesBuilder withPrefix(String prefix) {
        this.prefix = prefix
        return this
    }

    TagPropertiesBuilder withFallbackPrefixes(List<String> fallbackPrefixes) {
        this.fallbackPrefixes = fallbackPrefixes
        return this
    }

    TagPropertiesBuilder withSeparator(String separator) {
        this.separator = separator
        return this
    }
}
