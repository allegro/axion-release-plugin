package pl.allegro.tech.build.axion.release.util

import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.util.Pair

import static java.util.stream.Collectors.toList
import static TestEnvironment.setEnvVariable
import static TestEnvironment.unsetEnvVariable

class WithEnvironmentExtension implements IAnnotationDrivenExtension<WithEnvironment> {

    @Override
    void visitFeatureAnnotation(WithEnvironment annotation, FeatureInfo feature) {
        feature.getFeatureMethod().addInterceptor { invocation ->
            List<Pair<String, String>> envVarDefinitions = annotation.value().toList().stream()
                .map { it.split("=") }
                .map { Pair.of(it[0], it[1]) }
                .collect(toList())

            envVarDefinitions.forEach { setEnvVariable(it.first(), it.second()) }
            invocation.proceed()
            envVarDefinitions.forEach { unsetEnvVariable(it.first()) }
        }
    }
}
