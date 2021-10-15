package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties.*;

public class NextVersionConfig {
    @Input
    private String suffix = "alpha";

    @Input
    private String separator = "-";

    @Nested
    private Serializer serializer = NextVersionSerializer.DEFAULT.serializer;

    @Nested
    private Deserializer deserializer = NextVersionSerializer.DEFAULT.deserializer;

    public void setSerializer(String type) {
        this.serializer = NextVersionSerializer.find(type).serializer;
    }

    public void setSerializer(Serializer c) {
        this.serializer = c;
    }

    public void setDeserializer(String type) {
        this.deserializer = NextVersionSerializer.valueOf(type).deserializer;
    }

    public void setDeserializer(Deserializer c) {
        this.deserializer = c;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public Deserializer getDeserializer() {
        return deserializer;
    }
}
