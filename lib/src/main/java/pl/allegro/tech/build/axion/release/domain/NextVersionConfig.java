package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties.*;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public abstract class NextVersionConfig extends BaseExtension {
    private static final Logger logger = LoggerFactory.getLogger(NextVersionConfig.class);

    private static final String NEXT_VERSION_INCREMENTER_PROPERTY = "release.incrementer";
    private static final String NEXT_VERSION_PROPERTY = "release.version";
    private static final String DEPRECATED_NEXT_VERSION_PROPERTY = "release.nextVersion";

    @Inject
    public NextVersionConfig() {
        getSuffix().convention("alpha");
        getSeparator().convention("-");
        getSerializer().convention(NextVersionSerializer.DEFAULT.serializer);
        getDeserializer().convention(NextVersionSerializer.DEFAULT.deserializer);
    }

    @Input
    public abstract Property<String> getSuffix();

    @Input
    public abstract Property<String> getSeparator();

    @Internal
    public abstract Property<Serializer> getSerializer();

    @Internal
    public abstract Property<Deserializer> getDeserializer();

    public void serializer(String type) {
        getSerializer().set(NextVersionSerializer.find(type).serializer);
    }

    public void deserializer(String type) {
        getDeserializer().set(NextVersionSerializer.valueOf(type).deserializer);
    }

    public void serializer(Serializer serializer) {
        getSerializer().set(serializer);
    }

    public void deserializer(Deserializer deserializer) {
        getDeserializer().set(deserializer);
    }

    public NextVersionProperties nextVersionProperties() {

        if (getSuffix().get().isEmpty()) {
            String message = "scmVersion.nextVersion.suffix can't be empty! Empty suffix will prevent axion-release from distinguishing nextVersion from regular versions";
            throw new IllegalArgumentException(message);
        }

        return new NextVersionProperties(nextVersion().getOrNull(),
            getSuffix().get(),
            getSeparator().get(),
            versionIncrementerName().getOrNull(),
            getSerializer().get(),
            getDeserializer().get()
        );
    }

    private Provider<String> versionIncrementerName() {
        return gradleProperty(NEXT_VERSION_INCREMENTER_PROPERTY);
    }

    private Provider<String> nextVersion() {
        return gradleProperty(NEXT_VERSION_PROPERTY)
            .orElse(gradleProperty(DEPRECATED_NEXT_VERSION_PROPERTY)
                .map(it -> {
                    logger.warn("Using deprecated property: " + DEPRECATED_NEXT_VERSION_PROPERTY + "! Use " + NEXT_VERSION_PROPERTY + " instead.");
                    return it;
                }));
    }
}
