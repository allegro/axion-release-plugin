package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.util.FileLoader

import java.util.regex.Matcher
import java.util.regex.Pattern

class FileUpdateHook implements ReleaseHook {

    private final Map arguments

    FileUpdateHook(Map arguments) {
        this.arguments = arguments
    }

    @Override
    void act(HookContext hookContext) {
        File file = FileLoader.asFile(arguments.file)

        String text = file.text
        String replacedText = text.replaceAll(
                arguments.pattern(hookContext.previousVersion, hookContext.position),
                arguments.replacement(hookContext.currentVersion, hookContext.position)
        )

        file.write(replacedText)
        hookContext.addCommitPattern(file.canonicalPath)
    }

    static final class Factory extends DefaultReleaseHookFactory {
        
        @Override
        ReleaseHook create(Map arguments) {
            return new FileUpdateHook(arguments)
        }
    }

}
