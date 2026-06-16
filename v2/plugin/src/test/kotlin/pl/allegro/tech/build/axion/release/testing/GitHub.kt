package pl.allegro.tech.build.axion.release.testing

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(CiEnvironmentExtension::class)
annotation class GitHub(
    // Full ref, e.g. "refs/heads/main" or "refs/tags/v1.2.3"
    val ref: String = "refs/heads/main",
    // Short ref name; auto-derived from `ref` when left blank
    val refName: String = "",
    // "branch" or "tag"; auto-derived from `ref` when left blank
    val refType: String = "",
    val sha: String = "abc1234def56789",
    val repository: String = "owner/repo",
    val runId: String = "1"
)
