package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Selects the implementation used to read from the SCM.
 */
public enum ScmBackend {

    /**
     * Pure Java implementation, used by default.
     */
    JGIT("jgit"),

    /**
     * Native {@code git} executable. Only reads are executed natively, writes still go through JGit.
     */
    NATIVE("nativeGit");

    private final String id;

    ScmBackend(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ScmBackend of(String value) {
        if (value == null || value.isBlank()) {
            return JGIT;
        }
        String normalized = value.trim();
        return Arrays.stream(values())
            .filter(backend -> backend.id.equalsIgnoreCase(normalized) || backend.name().equalsIgnoreCase(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported scm backend '" + value + "', supported values: "
                    + Arrays.stream(values()).map(ScmBackend::getId).collect(Collectors.joining(", "))
            ));
    }
}
