package pl.allegro.tech.build.axion.release

import org.gradle.api.provider.Provider
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion
import pl.allegro.tech.build.axion.release.domain.BaseExtension
import spock.lang.Specification

@SuppressWarnings('GroovyAccessibility')
class BaseExtensionTest extends Specification {

    def "should not call forUseAtConfigurationTime for newer Gradle versions"() {
        given:
        ProviderFactory providers = Mock(ProviderFactory)
        FakeExtension extension = new FakeExtension(providers, GradleVersion.version("8.8"))
        Provider mockedProvider = Mock(Provider)

        when:
        extension.gradleProperty("name")

        then:
        1 * providers.gradleProperty("name") >> mockedProvider
        0 * mockedProvider.forUseAtConfigurationTime()
    }

    def "should call forUseAtConfigurationTime for older Gradle versions"() {
        given:
        ProviderFactory providers = Mock(ProviderFactory)
        FakeExtension extension = new FakeExtension(providers, null)
        Provider mockedProvider = Mock(Provider)

        when:
        extension.gradleProperty("name")

        then:
        1 * providers.gradleProperty("name") >> mockedProvider
        1 * mockedProvider.forUseAtConfigurationTime()
    }

    private class FakeExtension extends BaseExtension {
        ProviderFactory providers
        GradleVersion gradleVersion

        FakeExtension(ProviderFactory providers, GradleVersion gradleVersion) {
            this.providers = providers
            this.gradleVersion = gradleVersion
        }

        @Override
        protected ProviderFactory getProviders() {
            return providers
        }

        @Override
        protected ProjectLayout getLayout() {
            return null
        }

        @Override
        protected ObjectFactory getObjects() {
            return null
        }

        @Override
        protected GradleVersion currentGradleVersion() {
            if (gradleVersion == null) {
                return super.currentGradleVersion()
            }
            return gradleVersion
        }
    }
}
