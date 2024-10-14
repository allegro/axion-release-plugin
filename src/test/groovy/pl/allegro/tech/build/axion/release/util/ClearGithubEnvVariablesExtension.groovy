package pl.allegro.tech.build.axion.release.util

import org.spockframework.runtime.extension.IGlobalExtension

import static java.util.stream.Collectors.toList

class ClearGithubEnvVariablesExtension implements IGlobalExtension {

    @Override
    void start() {
        def keysToRemove = TestEnvironment.getEnvVariableNames().stream()
            .filter { variableName -> variableName.startsWith("GITHUB_") }
            .collect(toList())
        keysToRemove.forEach(TestEnvironment::unsetEnvVariable)
    }
}
