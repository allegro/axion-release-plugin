package pl.allegro.tech.build.axion.release.domain.scm;

import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver;
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger;

import java.util.List;
import java.util.Optional;

public class ScmService {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(ScmService.class);
    private final LocalOnlyResolver localOnlyResolver;
    private final ScmProperties scmProperties;
    private ScmRepository repository;

    public ScmService(LocalOnlyResolver localOnlyResolver, ScmProperties scmProperties, ScmRepository repository) {
        this.localOnlyResolver = localOnlyResolver;
        this.scmProperties = scmProperties;
        this.repository = repository;
    }

    public void tag(String tagName) {
        repository.tag(tagName);
    }

    public void tagOnCommit(String revision, String tagName) {
        repository.tagOnCommit(revision, tagName);
    }

    public void dropTag(String tagName) {
        try {
            repository.dropTag(tagName);
        } catch (ScmException e) {
            logger.quiet("Exception occurred during removing the tag: " + e.getMessage());
            throw e;
        }
    }

    public ScmPushResult push() {
        if (localOnlyResolver.localOnly(this.remoteAttached())) {
            logger.quiet("Changes made to local repository only");
            return new ScmPushResult(true, Optional.empty());
        }

        try {
            logger.quiet("Pushing all to remote: " + scmProperties.getRemote());
            return repository.push(scmProperties.getIdentity(), scmProperties.pushOptions());
        } catch (ScmException e) {
            logger.quiet("Exception occurred during push: " + e.getMessage());
            throw e;
        }

    }

    public ScmPosition position() {
        return repository.currentPosition();
    }

    /**
     * Find the last commit that occurred within a certain subdirectory of the project.
     * @param path Path to look at for commits
     * @param excludeSubFolders Subfolders within the search path to ignore, generally because they are subprojects themselves.
     * @return ScmPosition representing the last commit in the given search path.
     */
    public ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders) {
        return repository.positionOfLastChangeIn(path, excludeSubFolders);
    }

    public void commit(List patterns, String message) {
        repository.commit(patterns, message);
    }

    public boolean remoteAttached() {
        return repository.remoteAttached(scmProperties.getRemote());
    }

    public List<String> lastLogMessages(int messageCount) {
        return repository.lastLogMessages(messageCount);
    }
}
