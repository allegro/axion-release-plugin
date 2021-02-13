package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

class NextVersionConfig {

    @Input
    String suffix = 'alpha'

    @Input
    String separator = '-'

    @Nested
    Closure<String> serializer = NextVersionSerializer.DEFAULT.serializer

    @Nested
    Closure<String> deserializer = NextVersionSerializer.DEFAULT.deserializer

    void serializer(String type) {
        this.serializer = NextVersionSerializer.find(type).serializer
    }

    void serializer(Closure c) {
        this.serializer = c
    }

    void deserializer(String type) {
        this.deserializer = NextVersionSerializer.valueOf(type).deserializer
    }

    void deserializer(Closure c) {
        this.deserializer = c
    }
}
