package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class VersionResolver {
    
    private final ScmRepository repository

    private final VersionFactory versionFactory
    
    VersionResolver(ScmRepository repository, VersionFactory versionFactory) {
        this.repository = repository
        this.versionFactory = versionFactory
    }
    
    VersionWithPosition resolveVersion(VersionProperties versionRules, TagProperties tagRules, NextVersionProperties nextVersionRules) {
        Map positions = readPositions(tagRules, nextVersionRules)

        Version currentVersion = versionFactory.create(positions.currentPosition, versionRules, tagRules, nextVersionRules)
        VersionProperties disableForceRules = versionRules.withoutForce()
        Version previousVersion = versionFactory.create(positions.lastReleasePosition, disableForceRules, tagRules, nextVersionRules)

        ScmPosition position = positions.currentPosition.position
        if(positions.currentPosition.nextVersionTag) {
            position = position.asNotOnTagPosition()
        }
        
        return new VersionWithPosition(currentVersion, previousVersion, position)
    }


    private Map readPositions(TagProperties tagRules, NextVersionProperties nextVersionRules) {
        ScmPosition currentPosition = repository.currentPosition(~/^${tagRules.prefix}.*(|${nextVersionRules.suffix})$/)
        ScmPositionContext currentPositionContext = new ScmPositionContext(currentPosition, nextVersionRules)

        ScmPosition lastReleasePosition
        if(currentPositionContext.nextVersionTag) {
            lastReleasePosition = repository.currentPosition(~/^${tagRules.prefix}.*/, ~/.*${nextVersionRules.suffix}$/).asOnTagPosition()
        }
        else {
            lastReleasePosition = currentPosition.asOnTagPosition()
        }
        
        return [
                currentPosition: currentPositionContext,
                lastReleasePosition: new ScmPositionContext(lastReleasePosition, nextVersionRules)
        ]
    }
    
}
