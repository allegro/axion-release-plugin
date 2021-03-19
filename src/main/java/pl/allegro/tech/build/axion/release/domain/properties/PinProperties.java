package pl.allegro.tech.build.axion.release.domain.properties;

import java.io.File;

public class PinProperties {

    private final boolean enabled;
    private final File pinFile;

    public PinProperties(boolean enabled, File pinFile) {
        this.enabled = enabled;
        this.pinFile = pinFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public File getPinFile() {
        return pinFile;
    }
}
