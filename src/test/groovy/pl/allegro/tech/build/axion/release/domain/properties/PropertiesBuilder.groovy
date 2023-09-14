package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.ChecksConfig

class PropertiesBuilder {

    private TagProperties tagProperties = TagPropertiesBuilder.tagProperties().build()

    private NextVersionProperties nextVersionProperties = NextVersionPropertiesBuilder.nextVersionProperties().build()

    private VersionProperties versionProperties = VersionPropertiesBuilder.versionProperties().build()

    private HooksProperties hooksProperties = new HooksProperties([], [])

    private PropertiesBuilder() {
    }

    static PropertiesBuilder properties() {
        return new PropertiesBuilder()
    }

    Properties build() {
        ChecksConfig checksConfig = Fixtures.checksConfig(Fixtures.project())
        checksConfig.snapshotDependencies.set(false)
        return new Properties(
            false,
            versionProperties,
            tagProperties,
            checksConfig,
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
