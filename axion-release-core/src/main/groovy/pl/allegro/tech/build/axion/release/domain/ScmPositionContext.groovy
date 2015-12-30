package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class ScmPositionContext {
    
    final ScmPosition position
    
    final boolean nextVersionTag

    ScmPositionContext(ScmPosition position, NextVersionProperties nextVersionRules) {
        this.position = position
        this.nextVersionTag = afterNextVersionTag(position, nextVersionRules)
    }

    private static boolean afterNextVersionTag(ScmPosition position, NextVersionProperties nextVersionRules) {
        return position.latestTag?.endsWith(nextVersionRules.suffix)
    }
}
