package pl.allegro.tech.build.axion.release.domain.properties

class PropertiesBuilder {

    private TagProperties tagProperties = TagPropertiesBuilder.tagProperties().build()

    private NextVersionProperties nextVersionProperties = NextVersionPropertiesBuilder.nextVersionProperties().build()

    private VersionProperties versionProperties = VersionPropertiesBuilder.versionProperties().build()

    private ChecksProperties checksProperties = new ChecksProperties(true, true, false)

    private HooksProperties hooksProperties = new HooksProperties([], [])

    private PropertiesBuilder() {
    }

    static PropertiesBuilder properties() {
        return new PropertiesBuilder()
    }

    Properties build() {
        return new Properties(
                false,
                versionProperties,
                tagProperties,
                checksProperties,
                nextVersionProperties,
                hooksProperties
        )
    }

    PropertiesBuilder withVersionRules(VersionProperties versionRules) {
        this.versionProperties = versionRules
        return this
    }

    PropertiesBuilder withHooksRules(HooksProperties hooksRules) {
        this.hooksProperties = hooksRules
        return this
    }
}
