package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import groovy.transform.Canonical
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

@Canonical
class VersionIncrementerContext {

    Version currentVersion
    
    ScmPosition scmPosition
}
