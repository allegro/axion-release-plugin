package pl.allegro.tech.build.axion.release.testing

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import pl.allegro.tech.build.axion.release.git.ci.EnvironmentSource
import java.lang.reflect.AnnotatedElement

class CiEnvironmentExtension : BeforeEachCallback, ParameterResolver {

    override fun beforeEach(context: ExtensionContext) {
        val element = context.element.orElse(null) ?: return
        val env = buildEnvMap(element)
        context.getStore(NAMESPACE).put(ENV_KEY, MapEnvironmentSource(env))
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == EnvironmentSource::class.java ||
            parameterContext.parameter.type == MapEnvironmentSource::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
        extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(ENV_KEY) {
            MapEnvironmentSource(emptyMap())
        }

    private fun buildEnvMap(element: AnnotatedElement): Map<String, String> = buildMap {
        element.getAnnotation(GitHub::class.java)?.let { a ->
            val derivedRefName = a.refName.ifEmpty {
                when {
                    a.ref.startsWith("refs/heads/") -> a.ref.removePrefix("refs/heads/")
                    a.ref.startsWith("refs/tags/") -> a.ref.removePrefix("refs/tags/")
                    else -> a.ref.substringAfterLast("/")
                }
            }
            val derivedRefType = a.refType.ifEmpty { if (a.ref.startsWith("refs/tags/")) "tag" else "branch" }
            put("GITHUB_ACTIONS", "true")
            put("GITHUB_REF", a.ref)
            put("GITHUB_REF_NAME", derivedRefName)
            put("GITHUB_REF_TYPE", derivedRefType)
            put("GITHUB_SHA", a.sha)
            put("GITHUB_REPOSITORY", a.repository)
            put("GITHUB_RUN_ID", a.runId)
        }
        element.getAnnotation(GitLab::class.java)?.let { a ->
            put("GITLAB_CI", "true")
            put("CI_COMMIT_TAG", a.commitTag)
            put("CI_COMMIT_BRANCH", a.commitBranch)
            put("CI_COMMIT_REF_NAME", a.commitTag.ifEmpty { a.commitBranch })
            put("CI_COMMIT_SHA", a.commitSha)
            put("CI_PROJECT_PATH", a.projectPath)
        }
        element.getAnnotation(Jenkins::class.java)?.let { a ->
            put("JENKINS_URL", a.jenkinsUrl)
            put("GIT_BRANCH", a.gitBranch)
            put("GIT_TAG_NAME", a.gitTagName)
            put("GIT_COMMIT", a.gitCommit)
        }
    }

    companion object {
        private val NAMESPACE = Namespace.create(CiEnvironmentExtension::class.java)
        private const val ENV_KEY = "env"
    }
}
