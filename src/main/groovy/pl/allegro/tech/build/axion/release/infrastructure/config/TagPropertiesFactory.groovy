package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties

import java.util.regex.Pattern

class TagPropertiesFactory {

    static TagProperties create(TagNameSerializationConfig config, String currentBranch) {
        return new TagProperties(
                prefix: findPrefix(config, currentBranch),
                versionSeparator: config.versionSeparator,
                serialize: config.serialize,
                deserialize: config.deserialize,
                initialVersion: config.initialVersion
        )
    }

    private static String findPrefix(TagNameSerializationConfig config, String currentBranch) {
        String prefix = config.branchPrefix.findResult { pattern, prefix ->
            Pattern.compile(pattern).matcher(currentBranch).matches() ? prefix : null
        }

        return prefix ?: config.prefix
    }

}
