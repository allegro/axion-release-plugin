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
            def envVarDefinitions = annotation.value().toList().stream()
                .map {
                    def array = it.split("=")
                    Pair.of(array[0], array[1])
                }
                .collect(toList())
            envVarDefinitions.forEach { setEnvVariable(it.first(), it.second()) }
            invocation.proceed()
            envVarDefinitions.forEach { unsetEnvVariable(it.first()) }
        }
    }
}
