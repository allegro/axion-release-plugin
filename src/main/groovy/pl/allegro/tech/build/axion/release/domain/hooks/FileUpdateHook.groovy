package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.util.FileLoader

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
                arguments.pattern(hookContext.position, hookContext.previousVersion),
                arguments.replacement(hookContext.position, hookContext.currentVersion)
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
