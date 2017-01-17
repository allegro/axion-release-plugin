package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class HookContextBuilder {

    ScmService scmService

    ScmPosition position = new ScmPosition('master')

    String previousVersion = '1.0.0'

    String currentVersion = '2.0.0'

    HookContext build() {
        return new HookContext(
                null, null, scmService, position,
                new Version.Builder().setNormalVersion(previousVersion).build(),
                new Version.Builder().setNormalVersion(currentVersion).build())
    }
}
