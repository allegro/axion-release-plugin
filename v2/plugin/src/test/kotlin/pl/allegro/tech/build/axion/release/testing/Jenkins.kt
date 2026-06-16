package pl.allegro.tech.build.axion.release.testing

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(CiEnvironmentExtension::class)
annotation class Jenkins(
    // Includes remote prefix, e.g. "origin/main". Use BRANCH_NAME for multibranch.
    val gitBranch: String = "origin/main",
    // Empty for branch builds
    val gitTagName: String = "",
    val gitCommit: String = "abc1234def56789",
    val jenkinsUrl: String = "http://jenkins.example.com/"
)
