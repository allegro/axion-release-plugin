package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner;
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger;
import pl.allegro.tech.build.axion.release.domain.properties.Properties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult;
import pl.allegro.tech.build.axion.release.domain.scm.ScmService;

import java.util.Optional;

public class Releaser {

    private final ReleaseLogger logger = ReleaseLogger.Factory.logger(Releaser.class);
    private final VersionService versionService;
    private final ScmService repository;
    private final ReleaseHooksRunner hooksRunner;

    public Releaser(VersionService versionService, ScmService repository, ReleaseHooksRunner hooksRunner) {
        this.versionService = versionService;
        this.repository = repository;
        this.hooksRunner = hooksRunner;
    }

    public Optional<String> release(String projectRootRelativePath, Properties properties, boolean shouldForceIncrement) {
        VersionContext versionContext = versionService.currentVersion(
            properties.getVersion(), properties.getTag(), properties.getNextVersion()
        );
        Version version = versionContext.getVersion();

        if (versionContext.isSnapshot() || shouldForceIncrement) {
            String tagName = properties.getTag().getSerialize().call(properties.getTag(), version.toString());

            hooksRunner.runPreReleaseHooks(properties.getHooks(), properties, versionContext, version);

            logger.quiet("Creating tag: " + tagName);
            // if snapshot then release normally, otherwise release tag on last commit that is relevant to this project
            if (versionContext.isSnapshot()) {
                System.out.println("isSnapshot");
                repository.tag(tagName);
            } else {
                System.out.println("not isSnapshot");

                repository.tagOnCommit(repository.positionOfLastChangeIn(projectRootRelativePath,
                    properties.getVersion().getMonorepoProperties().getDirsToExclude()
                ).getRevision(), tagName);
            }

            hooksRunner.runPostReleaseHooks(properties.getHooks(), properties, versionContext, version);
            return Optional.of(tagName);
        } else {
            logger.quiet("Working on released version " + version + ", nothing to release");
            return Optional.empty();
        }
    }

    public ScmPushResult releaseAndPush(String projectRootRelativePath, Properties rules, boolean shouldForceIncrementVersion) {
        Optional<String> releasedTagName = release(projectRootRelativePath, rules, shouldForceIncrementVersion);

        ScmPushResult result = pushRelease();

        if (!result.isSuccess()) {
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
