package pl.allegro.tech.build.axion.release.domain

class NextVersionConfig {
    
    String suffix = 'alpha'

    String separator = '-'
    
    Closure serializer = NextVersionSerializer.DEFAULT.serializer
    
    Closure deserializer = NextVersionSerializer.DEFAULT.deserializer

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
