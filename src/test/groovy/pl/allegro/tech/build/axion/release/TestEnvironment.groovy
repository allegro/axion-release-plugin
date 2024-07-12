package pl.allegro.tech.build.axion.release

import org.spockframework.runtime.extension.IGlobalExtension

import static java.util.stream.Collectors.toList

class TestEnvironment implements IGlobalExtension {

    private static Map<String, String> MUTABLE_ENV_MAP

    @Override
    void start() {
        extractMutableMapFromSystemEnv()
        clearGithubActionsVariables()
    }

    private static void extractMutableMapFromSystemEnv() {
        def envMap = System.getenv()
        def envMapClass = envMap.getClass()
        def internalMapField = envMapClass.getDeclaredField("m")
        internalMapField.setAccessible(true)
        MUTABLE_ENV_MAP = (Map<String, String>) internalMapField.get(envMap)
    }

    private static void clearGithubActionsVariables() {
        def keysToRemove = MUTABLE_ENV_MAP.keySet().stream()
            .filter { variableName -> variableName.startsWith("GITHUB_") }
            .collect(toList())
        keysToRemove.forEach(MUTABLE_ENV_MAP::remove)
    }

    static void setEnvVariable(String name, String value) {
        MUTABLE_ENV_MAP.put(name, value)
    }

    static void unsetEnvVariable(String name) {
        MUTABLE_ENV_MAP.remove(name)
    }
}
