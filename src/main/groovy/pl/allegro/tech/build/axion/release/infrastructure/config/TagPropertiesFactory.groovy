package pl.allegro.tech.build.axion.release.infrastructure.config

import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties

import java.util.regex.Pattern

class TagPropertiesFactory {

    static TagProperties create(TagNameSerializationConfig config, String currentBranch) {
        return new TagProperties(
            findPrefix(config, currentBranch),
            config.versionSeparator.get(),
            config.serialize.get(),
            config.deserialize.get(),
            config.initialVersion.get()
        )
    }

    private static String findPrefix(TagNameSerializationConfig config, String currentBranch) {
        String prefix = config.branchPrefix.get().findResult { pattern, prefix ->
            Pattern.compile(pattern).matcher(currentBranch).matches() ? prefix : null
        }

        return prefix ?: config.prefix.get()
    }
}
