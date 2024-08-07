package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.util.stream.StreamSupport;

import static org.eclipse.jgit.revwalk.filter.RevFilter.NO_MERGES;

public class AutoDeepeningRevWalk extends RevWalk {

    private final Git jgitRepository;
    private final TransportConfigCallback transportConfigCallback;
    private final ObjectId startingCommit;
    private final boolean inclusive;
    private final Logger logger = Logging.getLogger(AutoDeepeningRevWalk.class);

    public AutoDeepeningRevWalk(Git jgitRepository, TransportConfigCallback transportConfigCallback, ObjectId startingCommit, boolean inclusive) {
        super(jgitRepository.getRepository());
        this.jgitRepository = jgitRepository;
        this.transportConfigCallback = transportConfigCallback;
        this.startingCommit = startingCommit;
        this.inclusive = inclusive;
    }

    @Override
    public RevCommit next() throws IOException {
        RevCommit commit = super.next();
        if (commit == null) {
            boolean deepened = deepenRepositoryBy(100);
            if (deepened) {
                startFromBeginning();
                return super.next();
            }
        }
        return commit;
    }

    private void startFromBeginning() throws IOException {
        dispose();
        markStart(parseCommit(startingCommit));
        if (!inclusive) {
            super.next();
        }
    }

    private boolean deepenRepositoryBy(int depth) {
        try {
            int numberOfCommits = countCommits();

            jgitRepository.fetch()
                .setDepth(numberOfCommits + depth)
                .setTransportConfigCallback(transportConfigCallback)
                .call();

            int numberOfCommitsAfterDeepening = countCommits();

            return numberOfCommitsAfterDeepening > numberOfCommits;

        } catch (GitAPIException e) {
            logger.warn("Unable to auto-deepen repository", e);
            return false;
        }
    }

    private int countCommits() throws GitAPIException {
        Iterable<RevCommit> commits = jgitRepository.log().setRevFilter(NO_MERGES).call();
        return (int) StreamSupport.stream(commits.spliterator(), false).count();
    }
}
