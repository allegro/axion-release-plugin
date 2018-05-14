package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.NextVersionSerializer

class NextVersionPropertiesBuilder {

    private String nextVersion

    private String versionIncrementer

    private String suffix = 'alpha'

    private NextVersionPropertiesBuilder(){
    }

    static NextVersionPropertiesBuilder nextVersionProperties() {
        return new NextVersionPropertiesBuilder()
    }

    NextVersionProperties build() {
        return new NextVersionProperties(
                nextVersion: nextVersion,
                suffix: suffix,
                versionIncrementer: versionIncrementer,
                separator: '-',
                deserializer: NextVersionSerializer.DEFAULT.deserializer,
                serializer: NextVersionSerializer.DEFAULT.serializer
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
