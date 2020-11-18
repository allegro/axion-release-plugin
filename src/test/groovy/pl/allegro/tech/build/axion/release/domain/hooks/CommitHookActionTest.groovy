package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class CommitHookActionTest extends RepositoryBasedTest {

    ScmService scmService

    def setup() {
        scmService = context.scmService()
    }

    def "should commit files matching patterns with given message"() {
        given:
        CommitHookAction hook = new CommitHookAction({ HookContext hookContext -> "test of version ${hookContext.currentVersion}" })
        HookContext context = new HookContextBuilder(scmService: scmService, previousVersion: '1.0.0', currentVersion: '2.0.0').build()
        context.addCommitPattern('*')

        when:
        hook.act(context)

        then:
        repository.lastLogMessages(1) == ['test of version 2.0.0']
    }
}
