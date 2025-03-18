package pl.allegro.tech.build.axion.release.domain.hooks;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import pl.allegro.tech.build.axion.release.domain.VersionService;
import pl.allegro.tech.build.axion.release.domain.properties.Properties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HookContext {
    private static final Logger logger = Logging.getLogger(HookContext.class);

    private final VersionService versionService;
    private final ScmService scmService;
    private final ScmPosition position;
    private String previousVersion;
    private String currentVersion;
    private final Properties rules;
    private final List<String> patternsToCommit = new ArrayList<>();

    public HookContext(
        Properties rules,
        VersionService versionService,
        ScmService scmService,
        ScmPosition position,
        Version previousVersion,
        Version currentVersion
    ) {
        this.versionService = versionService;
        this.rules = rules;
        this.scmService = scmService;
        this.position = position;
        this.previousVersion = previousVersion.toString();
        this.currentVersion = currentVersion.toString();
    }

    public String getReleaseVersion() {
        return currentVersion;
    }

    @Deprecated
    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public String readVersion() {
        String version = versionService.currentDecoratedVersion(
            rules.getVersion(), rules.getTag(), rules.getNextVersion()
        ).getDecoratedVersion();

        logger.info("Read version " + version + " from repository without using cache");
        return version;
    }

    public void commit(List patterns, String message) {
        scmService.commit(patterns, message);
    }

    public void addCommitPattern(String pattern) {
        patternsToCommit.add(pattern);
    }

    public void addCommitPattern(Collection<String> patterns) {
        patternsToCommit.addAll(patterns);
    }

    public void push() {
        scmService.push();
    }

    public static Logger getLogger() {
        return logger;
    }

    public final ScmPosition getPosition() {
        return position;
    }

    public final List<String> getPatternsToCommit() {
        return patternsToCommit;
    }
}
