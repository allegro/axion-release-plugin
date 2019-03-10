package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.util.FileLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class FileUpdateHookAction implements ReleaseHookAction {

    private final Map<String, Object> arguments;

    public FileUpdateHookAction(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void act(final HookContext hookContext) {
        arguments.computeIfAbsent("files", (k) -> Arrays.asList(arguments.get("file")));
        ((List<Object>) arguments.get("files")).forEach(f -> updateInFile(hookContext, f));
    }

    private void updateInFile(HookContext hookContext, Object potentialFile) {
        File file = FileLoader.asFile(potentialFile);
        String text = FileLoader.readFrom(potentialFile);

        String pattern = ((Closure) arguments.get("pattern"))
            .call(hookContext.getPreviousVersion(), hookContext).toString();

        String replacement = ((Closure) arguments.get("replacement"))
            .call(hookContext.getReleaseVersion(), hookContext).toString();

        try {
            hookContext.getLogger().quiet(
                "Replacing pattern \"" + pattern + "\" with \"" + replacement + "\" in " + file.getCanonicalPath()
            );

            String replacedText = Pattern.compile(pattern, Pattern.MULTILINE).matcher(text).replaceAll(replacement);

            write(file, replacedText);
            hookContext.addCommitPattern(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(File file, String text) {
        try (FileWriter fw = new FileWriter(file)) {
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            throw new FileUpdateHookException(e);
        }
    }

    private final static class FileUpdateHookException extends RuntimeException {
        FileUpdateHookException(Exception e) {
            super(e);
        }
    }

    public final static class Factory extends DefaultReleaseHookFactory {
        @Override
        public ReleaseHookAction create(Map<String, Object> arguments) {
            return new FileUpdateHookAction(arguments);
        }

    }
}
