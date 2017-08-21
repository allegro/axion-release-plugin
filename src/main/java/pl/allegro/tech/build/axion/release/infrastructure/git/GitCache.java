package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import java.util.HashMap;
import java.util.Map;

class GitCache {

    private final Map<String, CachedGitRepository> cachedRepositories = new HashMap<>();

    CachedGitRepository.CachedGitRevWalk walk(Repository repository, ObjectId startFrom) {
        String key = cacheKey(repository);

        synchronized (cachedRepositories) {
            if (!cachedRepositories.containsKey(key)) {
                cachedRepositories.put(key, new CachedGitRepository(repository));
            }
        }

        return cachedRepositories.get(key).walk(repository, startFrom);
    }

    void invalidate(Repository repository) {
        String key = cacheKey(repository);

        if (cachedRepositories.containsKey(key)) {
            cachedRepositories.get(key).invalidate(repository);
        }
    }

    private String cacheKey(Repository repository) {
        return repository.getDirectory().getAbsolutePath();
    }

}
