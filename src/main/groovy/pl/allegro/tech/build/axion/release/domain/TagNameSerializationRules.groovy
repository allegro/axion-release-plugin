package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class TagNameSerializationRules {

    static final String DEFAULT_VERSION_SEPARATOR = '-'

    static final String DEFAULT_PREFIX = 'release'

    String prefix = DEFAULT_PREFIX

    String versionSeparator = DEFAULT_VERSION_SEPARATOR

    Closure serialize = defaultSerializer()

    Closure deserialize = defaultDeserializer()

    Closure initialVersion = defaultInitialVersion()

    private static Closure defaultSerializer() {
        return { TagNameSerializationRules rules, String version ->
            return rules.prefix + rules.versionSeparator + version
        }
    }

    private static Closure defaultDeserializer() {
        return { TagNameSerializationRules rules, ScmPosition position ->
            return position.latestTag.substring(rules.prefix.length() + rules.versionSeparator.length())
        }
    }

    private static Closure defaultInitialVersion() {
        return { TagNameSerializationRules rules, ScmPosition position ->
            return '0.1.0'
        }
    }
}
