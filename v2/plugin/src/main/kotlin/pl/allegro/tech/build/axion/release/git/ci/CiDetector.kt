package pl.allegro.tech.build.axion.release.git.ci

object CiDetector {

    // Order matters: more specific / trusted providers first
    private val providers: List<CiProvider> = listOf(
        GithubActionsProvider(),
        GitlabCiProvider(),
        JenkinsProvider()
    )

    fun detect(env: EnvironmentSource): CiProvider? =
        providers.firstOrNull { it.isApplicable(env) }
}
