package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class ScmPositionContext {
    
    final ScmPosition position
    
    final boolean nextVersionTag

    ScmPositionContext(ScmPosition position, NextVersionConfig nextVersionConfig) {
        this.position = position
        this.nextVersionTag = afterNextVersionTag(position, nextVersionConfig)
    }

    private static boolean afterNextVersionTag(ScmPosition position, NextVersionConfig config) {
        return position.latestTag?.endsWith(config.suffix)
    }
}
