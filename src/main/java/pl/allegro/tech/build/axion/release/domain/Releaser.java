package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import pl.allegro.tech.build.axion.release.ReleaseBranchesConfiguration;
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner;
import pl.allegro.tech.build.axion.release.domain.properties.Properties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResultOutcome;
import pl.allegro.tech.build.axion.release.domain.scm.ScmService;

import java.util.Optional;

public class Releaser {

    private static final Logger logger = Logging.getLogger(Releaser.class);
    private final VersionService versionService;
    private final ScmService repository;
    private final ReleaseHooksRunner hooksRunner;

    public Releaser(VersionService versionService, ScmService repository, ReleaseHooksRunner hooksRunner) {
        this.versionService = versionService;
        this.repository = repository;
        this.hooksRunner = hooksRunner;
    }

    public Optional<String> release(Properties properties, ReleaseBranchesConfiguration releaseBranchesConfiguration) {
        if (releaseBranchesConfiguration.shouldRelease()) {
            String message = String.format(
                "Release step skipped since 'releaseOnlyOnDefaultBranches' option is set, and '%s' was not in 'releaseBranchNames' list [%s]",
                releaseBranchesConfiguration.getCurrentBranch(),
                String.join(",", releaseBranchesConfiguration.getReleaseBranchNames())
            );
            logger.quiet(message);
            return Optional.empty();
        }

        VersionContext versionContext = versionService.currentVersion(
            properties.getVersion(), properties.getTag(), properties.getNextVersion()
        );

        Version version = versionContext.getVersion();

        if (versionContext.isSnapshot()) {
            String tagName = properties.getTag().getSerialize().apply(properties.getTag(), version.toString());

            hooksRunner.runPreReleaseHooks(properties.getHooks(), properties, versionContext, version);

            logger.quiet("Creating tag: " + tagName);
            repository.tag(tagName);

            hooksRunner.runPostReleaseHooks(properties.getHooks(), properties, versionContext, version);
            return Optional.of(tagName);
        } else {
            logger.quiet("Working on released version " + version + ", nothing to release");
            return Optional.empty();
        }
    }

    public ScmPushResult releaseAndPush(Properties rules, ReleaseBranchesConfiguration releaseBranchesConfiguration) {
        Optional<String> releasedTagName = release(rules, releaseBranchesConfiguration);

        if (releasedTagName.isEmpty()) {
            return new ScmPushResult(ScmPushResultOutcome.SKIPPED, Optional.empty(), Optional.empty());
        }

        ScmPushResult result = pushRelease();

        if (result.getOutcome().equals(ScmPushResultOutcome.FAILED)) {
            releasedTagName.ifPresent(this::rollbackRelease);
        }

        return result;
    }

    public ScmPushResult pushRelease() {
        return repository.push();
    }

    private void rollbackRelease(String tagName) {
        logger.quiet("Removing tag: " + tagName);
        repository.dropTag(tagName);
        logger.quiet("Tag " + tagName + " removed");
    }
}
