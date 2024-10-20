package pl.allegro.tech.build.axion.release.infrastructure.github;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GithubFacade {

    private static final Logger logger = Logging.getLogger(GithubFacade.class);
    private static final String GITHUB_ACTIONS = "GITHUB_ACTIONS";
    private static final String GITHUB_OUTPUT = "GITHUB_OUTPUT";

    public static void setOutputIfNotAlreadySet(String name, String value) {
        if (System.getenv().containsKey(GITHUB_ACTIONS)) {
            try {
                boolean alreadySet = Files.readAllLines(githubOutputPath()).stream()
                    .anyMatch(line -> line.startsWith(name + "="));

                if (!alreadySet) {
                    setOutput(name, value);
                }
            } catch (IOException e) {
                logger.warn("Unable to the verify whether '{}' GitHub output is already set, cause: {}", name, e.getMessage());
            }
        }
    }

    public static void setOutput(String name, String value) {
        if (System.getenv().containsKey(GITHUB_ACTIONS)) {
            try {
                Files.write(
                    githubOutputPath(),
                    String.format("%s=%s\n", name, value).getBytes(),
                    StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                logger.warn("Unable to the set '{}' GitHub output, cause: {}", name, e.getMessage());
            }
        }
    }

    private static Path githubOutputPath() {
        return Paths.get(System.getenv(GITHUB_OUTPUT));
    }
}
