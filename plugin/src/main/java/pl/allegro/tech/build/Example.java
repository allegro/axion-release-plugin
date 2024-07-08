package pl.allegro.tech.build;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.environment.GitEnvironment;
import pl.allegro.tech.build.incrementer.SimpleVersionIncrementer;
import pl.allegro.tech.build.incrementer.VersionIncrementer;

public class Example {
    public static void main(String[] args) {
        VersionIncrementer versionIncrementer = new SimpleVersionIncrementer();
        GitEnvironment environment = EnvironmentStrategy.getEnvironment(System.getenv());
        System.out.println(environment.getDefaultBranchName());
        System.out.println(environment.getCurrentBranchName());
        System.out.println(environment.getHeadCommit());
        System.out.println(environment.getCurrentVersion());
        Version nextVersion = versionIncrementer.getNextVersion(environment.getCurrentVersion());
        if (environment.getDefaultBranchName().equals(environment.getCurrentBranchName())) {
            System.out.println(nextVersion);
        } else {
            System.out.println(nextVersion.toBuilder().setPreReleaseVersion("SNAPSHOT").build());
        }
    }
}
