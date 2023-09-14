package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

abstract class TagNameSerializationConfig extends BaseExtension {

    TagNameSerializationConfig() {
        serialize.convention(TagNameSerializer.DEFAULT.serializer)
        deserialize.convention(TagNameSerializer.DEFAULT.deserializer)
        initialVersion.convention(defaultInitialVersion())
        prefix.convention(TagPrefixConf.defaultPrefix())
        versionSeparator.convention(TagPrefixConf.defaultSeparator())
    }

    @Input
    abstract Property<String> getPrefix()

    @Input
    abstract MapProperty<String, String> getBranchPrefix()

    @Input
    abstract Property<String> getVersionSeparator()

    @Internal
    abstract Property<TagProperties.Serializer> getSerialize()

    @Internal
    abstract Property<TagProperties.Deserializer> getDeserialize()

    @Internal
    abstract Property<TagProperties.InitialVersionSupplier> getInitialVersion()

    @Deprecated
    void setSerializer(TagProperties.Serializer serializer) {
        this.serialize.set(serializer)
    }

    void serializer(TagProperties.Serializer serializer) {
        this.serialize.set(serializer)
    }

    @Deprecated
    void setDeserializer(TagProperties.Deserializer deserializer) {
        this.deserialize.set(deserializer)
    }

    void deserializer(TagProperties.Deserializer deserializer) {
        this.deserialize.set(deserializer)
    }

    void initialVersion(TagProperties.InitialVersionSupplier versionSupplier){
        this.initialVersion.set(versionSupplier)
    }

    private static TagProperties.InitialVersionSupplier defaultInitialVersion() {
        return { TagProperties rules, ScmPosition position ->
            return '0.1.0'
        }
    }
}
