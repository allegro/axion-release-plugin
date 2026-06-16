package pl.allegro.tech.build.axion.release.testing

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(CiEnvironmentExtension::class)
annotation class GitLab(
    // Set for tag builds, empty for branch builds
    val commitTag: String = "",
    // Set for branch builds, empty for tag builds
    val commitBranch: String = "main",
    val commitSha: String = "abc1234def56789",
    val projectPath: String = "group/repo"
)
