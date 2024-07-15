package pl.allegro.tech.build.axion.release.util

class TestEnvironment {

    private static Map<String, String> MUTABLE_ENV_MAP

    static  {
        extractMutableMapFromSystemEnv()
    }

    private static void extractMutableMapFromSystemEnv() {
        def envMap = System.getenv()
        def envMapClass = envMap.getClass()
        def internalMapField = envMapClass.getDeclaredField("m")
        internalMapField.setAccessible(true)
        MUTABLE_ENV_MAP = (Map<String, String>) internalMapField.get(envMap)
    }

    static void setEnvVariable(String name, String value) {
        MUTABLE_ENV_MAP.put(name, value)
    }

    static void unsetEnvVariable(String name) {
        MUTABLE_ENV_MAP.remove(name)
    }

    static Set<String> getEnvVariableNames() {
        return MUTABLE_ENV_MAP.keySet()
    }
}
