package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import java.util.regex.Pattern

class VersionPropertiesFactory {

    static VersionProperties create(VersionConfig config, String currentBranch) {
        return new VersionProperties(
            config.forcedVersion().getOrNull(),
            config.forceSnapshot().get(),
            config.ignoreUncommittedChanges().get(),
            findVersionCreator(config, currentBranch),
            config.snapshotCreator.get(),
            findVersionIncrementer(config, currentBranch),
            config.sanitizeVersion.get(),
            config.useHighestVersion().get(),
            config.monorepoConfig
        )
    }

    private static VersionProperties.Incrementer findVersionIncrementer(VersionConfig config, String currentBranch) {
        if( config.versionIncrementerType().isPresent()) {
           return PredefinedVersionIncrementer.versionIncrementerFor(config.versionIncrementerType().get(), [:])
        }

        return findVersionIncrementer(
            currentBranch,
            config.branchVersionIncrementer.get(),
            { VersionIncrementerContext a -> config.versionIncrementer.get().apply(a) },
            { v ->
                if (v instanceof List) {
                    return PredefinedVersionIncrementer.versionIncrementerFor(v[0], v[1])
                }
                return PredefinedVersionIncrementer.versionIncrementerFor(v, [:])
            }
        )
    }

    private static VersionProperties.Creator findVersionCreator(VersionConfig config, String currentBranch) {
        if (config.versionCreatorType().isPresent()) {
            return PredefinedVersionCreator.versionCreatorFor(config.versionCreatorType().get())
        }

        return find(
            currentBranch,
            config.branchVersionCreator.get(),
            config.versionCreator.get(),
            { String s -> PredefinedVersionCreator.versionCreatorFor(s) }
        )
    }

    private
    static VersionProperties.Incrementer findVersionIncrementer(String currentBranch,
                                                                Map<String, Object> collection,
                                                                VersionProperties.Incrementer defaultValue,
                                                                Closure<VersionProperties.Incrementer> converter) {
        Object value = collection?.findResult { pattern, value ->
            Pattern.matches(pattern, currentBranch) ? value : null
        }

        if (value == null) {
            return defaultValue
        } else if (!(value instanceof Closure)) {
            return converter.call(value)
        } else {
            return value
        }
    }

    private
    static VersionProperties.Creator find(String currentBranch, Map<String, Object> collection, VersionProperties.Creator defaultValue, Closure<VersionProperties.Creator> converter) {
        Object value = collection?.findResult { pattern, value ->
            Pattern.matches(pattern, currentBranch) ? value : null
        }

        if (value == null) {
            return defaultValue
        } else if (!(value instanceof Closure)) {
            return converter.call(value)
        } else {
            return value
        }
    }
}
