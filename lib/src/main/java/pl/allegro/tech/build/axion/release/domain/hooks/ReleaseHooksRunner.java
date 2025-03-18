package pl.allegro.tech.build.axion.release.domain.hooks;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.VersionContext;
import pl.allegro.tech.build.axion.release.domain.VersionService;
import pl.allegro.tech.build.axion.release.domain.properties.HooksProperties;
import pl.allegro.tech.build.axion.release.domain.properties.Properties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmService;

public class ReleaseHooksRunner {

    private final VersionService versionService;
    private final ScmService scmService;

    public ReleaseHooksRunner(VersionService versionService, ScmService scmService) {
        this.versionService = versionService;
        this.scmService = scmService;
    }

    public void runPreReleaseHooks(HooksProperties hooksRules, Properties rules, VersionContext versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(
            rules, versionService, scmService, versionWithPosition.getPosition(), versionWithPosition.getPreviousVersion(), releaseVersion
        );

        hooksRules.getPreReleaseHooks().forEach(h -> h.act(context));
    }

    public void runPostReleaseHooks(HooksProperties hooksRules, Properties rules, VersionContext versionWithPosition, Version releaseVersion) {
        final HookContext context = new HookContext(
            rules, versionService, scmService, versionWithPosition.getPosition(), versionWithPosition.getPreviousVersion(), releaseVersion
        );

        hooksRules.getPostReleaseHooks().forEach(h -> h.act(context));
    }
}
