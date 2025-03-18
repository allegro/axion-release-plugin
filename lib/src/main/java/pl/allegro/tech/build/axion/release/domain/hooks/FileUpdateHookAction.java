package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.util.FileLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        Charset charset = Optional.ofNullable(arguments.get("encoding"))
            .map(Object::toString)
            .map(Charset::forName)
            .orElseGet(Charset::defaultCharset);

        try {
            hookContext.getLogger().quiet(
                "Replacing pattern \"" + pattern + "\" with \"" + replacement + "\" in " + file.getCanonicalPath()
            );

            String replacedText = Pattern.compile(pattern, Pattern.MULTILINE).matcher(text).replaceAll(replacement);

            write(file, replacedText, charset);
            hookContext.addCommitPattern(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(File file, String text, Charset charset) {
        try {
            Files.write(file.toPath(), text.getBytes(charset));
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
