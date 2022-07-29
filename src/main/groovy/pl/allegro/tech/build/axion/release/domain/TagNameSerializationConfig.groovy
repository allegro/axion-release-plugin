package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class TagNameSerializationConfig {

    @Input
    String prefix

    @Input
    Map<String, String> branchPrefix = [:]

    @Input
    String versionSeparator

    @Nested
    TagProperties.Serializer serialize = TagNameSerializer.DEFAULT.serializer

    void setSerializer(TagProperties.Serializer serializer) {
        this.serialize = serializer
    }

    @Nested
    TagProperties.Deserializer deserialize = TagNameSerializer.DEFAULT.deserializer

    void setDeserializer(TagProperties.Deserializer deserializer) {
        this.deserialize = deserializer
    }

    @Nested
    TagProperties.InitialVersionSupplier initialVersion = defaultInitialVersion()

    private static TagProperties.InitialVersionSupplier defaultInitialVersion() {
        return { TagProperties rules, ScmPosition position ->
            return '0.1.0'
        }
    }
}
