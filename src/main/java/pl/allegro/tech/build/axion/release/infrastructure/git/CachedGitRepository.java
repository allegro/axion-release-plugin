package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class CachedGitRepository {

    /**
     * Since this repository is meant to be kept as a static object, and Gradle Demon might keep JVM process
     * in background for a long time, we have to put some timer on this repo.
     *
     * Head ObjectId check is not sufficient, since it would not detect operations like adding a new tag from command
     * line. This could lead to some nasty bugs. Thus we will keep cache for 60 seconds, which seems like a reasonable
     * amount of time fro builds to complete.
     *
     * In case build in mutli-module project lasts longer, i don't think second added by Axion makes a huge difference.
     */
    private static final long INVALIDATE_AFTER_MILLIS = 1000 * 60;

    private long refreshedAt;

    /**
     * HEAD of repo for which this cache was created. Cache has to be invalidated
     * if HEAD changes.
     * I assume that changes to the head are rare and do not occur concurrently with
     * plugin running, so concurrency issues are totally ignored.
     */
    private ObjectId cachedHead;

    private final SynchronizedPreloadingGitRevWalkCache revWalkCache;

    CachedGitRepository(Repository repository) {
        this.cachedHead = currentHead(repository);
        this.revWalkCache = new SynchronizedPreloadingGitRevWalkCache(repository, cachedHead);
    }

    private ObjectId currentHead(Repository repository) {
        try {
            return repository.resolve(Constants.HEAD);
        } catch (IOException exception) {
            throw new GitException("Failed to resolve HEAD", exception);
        }
    }

    private synchronized void invalidateCacheIfHeadChanged(Repository repository) {
        ObjectId currentHead = currentHead(repository);
        long currentTime = System.currentTimeMillis();
        if (!currentHead.equals(cachedHead) || refreshedAt + INVALIDATE_AFTER_MILLIS > currentTime) {
            revWalkCache.invalidate(repository, currentHead);
            refreshedAt = currentTime;
            cachedHead = currentHead;
        }
    }

    void invalidate(Repository repository) {
        revWalkCache.invalidate(repository, currentHead(repository));
    }

    CachedGitRevWalk walk(Repository repository, ObjectId startFrom) {
        invalidateCacheIfHeadChanged(repository);
        return new CachedGitRevWalk(revWalkCache).start(startFrom);
    }

    static class SynchronizedPreloadingGitRevWalkCache {

        private static final String GIT_TAG_PREFIX = "refs/tags/";

        private static final int TAGS_TO_LOAD_BASE = 1;

        private int tagsToLoadExponent = 0;

        private boolean nothingMoreLeft = false;

        private ObjectId headId;

        private final ArrayList<TagsOnCommit> cache = new ArrayList<>();

        private RevWalk revWalk;

        private final Map<String, List<String>> commitsWithTagsCache = new HashMap<>();

        SynchronizedPreloadingGitRevWalkCache(Repository repository, ObjectId headId) {
            invalidate(repository, headId);
        }

        synchronized void invalidate(Repository repository, ObjectId headId) {
            cache.clear();
            nothingMoreLeft = false;
            tagsToLoadExponent = 0;
            this.headId = headId;

            try {
                revWalk = new RevWalk(repository);
                revWalk.sort(RevSort.NONE);
                revWalk.markStart(revWalk.parseCommit(headId));
            } catch (IOException exception) {
                throw new GitException("Failed to start commit walk", exception);
            }

            populateTagsCache(repository);
        }

        TagsOnCommit get(int index) {
            try {
                String lastCachedCommitId = lastCachedCommitId();
                while (
                    index >= cache.size()
                        && !nothingMoreLeft
                        && !loadMore(lastCachedCommitId)
                    ) {
                    lastCachedCommitId = lastCachedCommitId();
                }
            } catch (IOException exception) {
                throw new GitException("Failed to load commits into cache", exception);
            }

            if (index >= cache.size()) {
                return null;
            }
            return cache.get(index);
        }

        private String lastCachedCommitId() {
            return cache.isEmpty() ? null : cache.get(cache.size() - 1).getCommitId();
        }


        private synchronized boolean loadMore(String expectedLastCachedCommitId) throws IOException {
            // check if there was no other parallel loadMore() call
            if (!Objects.equals(lastCachedCommitId(), expectedLastCachedCommitId)) {
                return false;
            }

            // decide how many tags to load
            // number of loaded tags grows exponentially with each load
            // the assumption is, most project have version tags very close in history, so loading
            // latest tag should be enough, but if we struggle to find matching tags than
            // it is beneficial to load more and more at once, since this is probably some very
            // non standard project
            int tagsToLoad = TAGS_TO_LOAD_BASE * (int) Math.pow(2, tagsToLoadExponent);
            tagsToLoadExponent++;

            int i;
            for (i = 0; i < tagsToLoad; ) {
                RevCommit commit = revWalk.next();
                if (commit == null) {
                    break;
                }

                String commitHash = commit.getId().getName();

                if (commitsWithTagsCache.containsKey(commitHash)) {
                    cache.add(new TagsOnCommit(commitHash, commitsWithTagsCache.get(commitHash), Objects.equals(headId, commit.getId())));
                    i++;
                }
            }

            // if we actually loaded less tags than we intended to, there is nothing left in repo
            nothingMoreLeft = i < tagsToLoad;
            if (nothingMoreLeft) {
                revWalk.dispose();
            }
            return true;
        }

        private void populateTagsCache(Repository repository) {
            commitsWithTagsCache.clear();

            Collection<Ref> tags = repository.getTags().values();

            try {
                for (Ref tag : tags) {
                    String tagShortName = tag.getName().substring(GIT_TAG_PREFIX.length());
                    String commitId = revWalk.parseCommit(tag.getObjectId()).getName();

                    if (!commitsWithTagsCache.containsKey(commitId)) {
                        commitsWithTagsCache.put(commitId, new ArrayList<String>());
                    }

                    List<String> tagsOnCommit = commitsWithTagsCache.get(commitId);
                    tagsOnCommit.add(tagShortName);
                }
            } catch (IOException exception) {
                throw new GitException("Failed to initialize Git tags cache", exception);
            }
        }
    }

    static class CachedGitRevWalk {

        private final SynchronizedPreloadingGitRevWalkCache cache;

        private boolean started = false;

        private int currentPosition = 0;

        CachedGitRevWalk(SynchronizedPreloadingGitRevWalkCache cache) {
            this.cache = cache;
        }

        CachedGitRevWalk start(ObjectId startFrom) {
            String startFromString = startFrom.toString();
            this.started = true;

//            TagsOnCommit tagsOnCommit;
//            do {
//                tagsOnCommit = next();
//            } while (tagsOnCommit != null && !tagsOnCommit.getCommitId().equals(startFromString));
            return this;
        }

        TagsOnCommit next() {
            if (!started) {
                throw new IllegalStateException("Can't call next() on unstarted walk. Call start() to initialize the walk.");
            }

            // read next tagsOnCommit either from cache or from revwalk
            TagsOnCommit tagsOnCommit = cache.get(currentPosition);
            currentPosition++;

            return tagsOnCommit;
        }
    }

    static class GitException extends RuntimeException {
        GitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
