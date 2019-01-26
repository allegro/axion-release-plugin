package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.NextVersionSerializer

class NextVersionPropertiesBuilder {

    private String nextVersion

    private String versionIncrementer

    private String suffix = 'alpha'

    private NextVersionPropertiesBuilder() {
    }

    static NextVersionPropertiesBuilder nextVersionProperties() {
        return new NextVersionPropertiesBuilder()
    }

    NextVersionProperties build() {
        return new NextVersionProperties(
            nextVersion,
            suffix,
            '-',
            versionIncrementer,
            NextVersionSerializer.DEFAULT.serializer,
            NextVersionSerializer.DEFAULT.deserializer
        )
    }

    NextVersionPropertiesBuilder withNextVersion(String nextVersion) {
        this.nextVersion = nextVersion
        return this
    }

    NextVersionPropertiesBuilder withVersionIncrementer(String incrementer) {
        this.versionIncrementer = incrementer
        return this
    }

    NextVersionPropertiesBuilder withSuffix(String suffix) {
        this.suffix = suffix
        return this
    }
}
