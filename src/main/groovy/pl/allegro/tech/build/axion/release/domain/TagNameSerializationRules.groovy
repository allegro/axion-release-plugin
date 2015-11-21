package pl.allegro.tech.build.axion.release.domain

import java.util.regex.Pattern

class TagNameSerializationRules {

    final String prefix

    final String versionSeparator

    TagNameSerializationRules(String prefix, String versionSeparator) {
        this.prefix = prefix
        this.versionSeparator = versionSeparator
    }

    static TagNameSerializationRules calculate(TagNameSerializationConfig config, String currentBranch) {
         String prefix = config.branchPrefix.findResult { pattern, prefix ->
            Pattern.compile(pattern).matcher(currentBranch).matches() ? prefix : null
        }

        prefix = prefix ?: config.prefix

        return new TagNameSerializationRules(prefix, config.versionSeparator)
    }
}
