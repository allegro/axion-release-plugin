package pl.allegro.tech.build.environment;

import com.github.zafarkhaja.semver.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import pl.allegro.tech.build.Defaults;

import java.io.File;
import java.io.IOException;

/**
 * Based on local git repository.
 * Works by executing git commands directly (jgit)
 * Default fallback if no CI environment is detected
 */
public class LocalEnvironment implements GitEnvironment {

    private final Git git;

    public LocalEnvironment() {
        try (Git git = Git.open(new File("."))) {
            this.git = git;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDefaultBranchName() {
        return "main";
    }

    @Override
    public String getCurrentBranchName() {
        try {
            return git.getRepository().getBranch();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHeadCommit() {
        try {
            return git.getRepository().resolve("HEAD").toObjectId().getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Version getCurrentVersion() {
        try {
            String latestTag = git.describe().setTags(true).setAbbrev(0).setMatch(Defaults.tagPrefix + "*").call();
            return Version.parse(latestTag.replaceFirst(Defaults.tagPrefix, ""));
        } catch (GitAPIException | InvalidPatternException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAhead() {
        try {
            BranchTrackingStatus status = BranchTrackingStatus.of(git.getRepository(), getCurrentBranchName());
            if (status == null) throw new IllegalStateException("Branch not tracking any remote");
            return status.getAheadCount() != 0 || status.getBehindCount() != 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
