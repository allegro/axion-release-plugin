package pl.allegro.tech.build.axion.release.infrastructure.github;

import groovy.json.JsonBuilder;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public abstract class GithubService implements BuildService<BuildServiceParameters.None>, AutoCloseable {

    private static final Logger logger = Logging.getLogger(GithubService.class);
    private static final String GITHUB_ACTIONS = "GITHUB_ACTIONS";
    private static final String GITHUB_OUTPUT = "GITHUB_OUTPUT";

    private final Map<String, Map<String, String>> outputs = Collections.synchronizedMap(new LinkedHashMap<>());

    public void setOutput(String name, String projectName, String value) {
        outputs.putIfAbsent(name, Collections.synchronizedMap(new LinkedHashMap<>()));
        outputs.get(name).put(projectName, value);
    }

    @Override
    public void close() {
        if (System.getenv().containsKey(GITHUB_ACTIONS)) {

            outputs.forEach((name, valuePerProject) -> {
                List<String> distinctValues = valuePerProject.values().stream()
                    .distinct()
                    .collect(toList());

                if (distinctValues.size() == 1) {
                    String singleValue = distinctValues.get(0);
                    writeOutput(name, singleValue);
                } else {
                    String jsonValue = new JsonBuilder(valuePerProject).toString();
                    logger.warn("Multiple values provided for the '{}' GitHub output, it will be formatted as JSON: {}", name, jsonValue);
                    writeOutput(name, jsonValue);
                }
            });
        }
    }

    private static void writeOutput(String name, String value) {
        try {
            Files.write(
                Paths.get(System.getenv(GITHUB_OUTPUT)),
                String.format("%s=%s\n", name, value).getBytes(),
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            logger.warn("Unable to the set '{}' GitHub output, cause: {}", name, e.getMessage());
        }
    }
}
