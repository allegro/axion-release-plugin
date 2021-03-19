package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.PinConfig
import pl.allegro.tech.build.axion.release.domain.properties.PinProperties

class PinPropertiesFactory {

    static PinProperties create(PinConfig config) {
        return new PinProperties(config.enabled().get(), config.pinFile?.getAsFile()?.get())
    }

}
