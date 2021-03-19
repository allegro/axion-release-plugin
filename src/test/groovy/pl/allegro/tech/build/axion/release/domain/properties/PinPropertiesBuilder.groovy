package pl.allegro.tech.build.axion.release.domain.properties

class PinPropertiesBuilder {

    private boolean enabled = false;

    private File pinFile;

    private PinPropertiesBuilder() {
    }

    static PinPropertiesBuilder pinProperties() {
        return new PinPropertiesBuilder()
    }

    PinProperties build() {
        return new PinProperties(
            enabled,
            pinFile
        )
    }

    PinPropertiesBuilder setEnabled(boolean enabled) {
        this.enabled = enabled
        return this
    }


    PinPropertiesBuilder pinFile(File pinFile) {
        this.pinFile = pinFile
        return this
    }
}
