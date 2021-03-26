package pl.allegro.tech.build.axion.release.infrastructure.git;

import groovy.lang.Tuple2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import pl.allegro.tech.build.axion.release.domain.scm.ScmException;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides cached version for some operations on {@link ScmRepository}
 */
public class ScmCache {

    /**
     * Since this cache is statis and Gradle Demon might keep JVM process in background for a long
     * time, we have to put some TTL for cached values.
     */
    private static final long INVALIDATE_AFTER_MILLIS = 1000 * 60;

    private static final ScmCache CACHE = new ScmCache();

    public static ScmCache getInstance() {
        return CACHE;
    }

    private ScmCache() { }

    private final Map<String, CachedState> cache = new HashMap<>();

    synchronized void invalidate(ScmRepository repository) {
        cache.remove(repository.id());
    }

    public synchronized boolean checkUncommittedChanges(ScmRepository repository) {
        CachedState state = retrieveCachedStateFor(repository);
        if (state.hasUncommittedChanges == null) {
            state.hasUncommittedChanges = repository.checkUncommittedChanges();
        }
        return state.hasUncommittedChanges;
    }

    synchronized List<Tuple2<Ref, RevCommit>> parsedTagList(ScmRepository repository, Git git, RevWalk walker) throws GitAPIException {
        CachedState state = retrieveCachedStateFor(repository);
        if (state.tags == null) {
            List<Tuple2<Ref, RevCommit>> list = new ArrayList<>();
            for (Ref tag : git.tagList().call()) {
                try {
                    list.add(new Tuple2<>(tag, walker.parseCommit(tag.getObjectId())));
                } catch (IOException e) {
                    throw new ScmException(e);
                }
            }
            state.tags = list;
        }
        return state.tags;
    }

    private CachedState retrieveCachedStateFor(ScmRepository scmRepository) {
        String key = scmRepository.id();
        String currentHeadRevision = scmRepository.currentPosition().getRevision();
        long currentTime = System.currentTimeMillis();
        CachedState state = cache.get(key);
        if (state == null) {
            state = new CachedState(currentHeadRevision);
            cache.put(key, state);
        } else {
            if (!currentHeadRevision.equals(state.headRevision) || (state.createTimestamp + INVALIDATE_AFTER_MILLIS) < currentTime) {
                state = new CachedState(currentHeadRevision);
                cache.put(key, state);
            }
        }
        return state;
    }

    /**
     * Helper object holding cached values per SCM repository
     */
    private static class CachedState {

        final long createTimestamp;

        /**
         * HEAD revision of repo for which this cache was created. Cache has to be invalidated
         * if HEAD changes.
         */
        final String headRevision;

        Boolean hasUncommittedChanges;

        List<Tuple2<Ref, RevCommit>> tags;

        CachedState(String headRevision) {
            createTimestamp = System.currentTimeMillis();
            this.headRevision = headRevision;
        }
    }
}
