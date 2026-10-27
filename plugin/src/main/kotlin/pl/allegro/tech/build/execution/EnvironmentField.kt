package pl.allegro.tech.build.execution

/** Stable boundary fields; actual environment names belong to their provider's bindings. */
internal enum class EnvironmentField {
    API_URL,
    CI_HINT,
    COMMIT_TAG,
    DEFAULT_BRANCH,
    EVENT_NAME,
    EVENT_PAYLOAD,
    REF_NAME,
    REF_TYPE,
    REPOSITORY_ID,
    REVISION,
    TOKEN,
}
