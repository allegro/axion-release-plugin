package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.util.FileLoader

class FileUpdateHook implements ReleaseHook {

    private final Map arguments

    FileUpdateHook(Map arguments) {
        this.arguments = arguments
    }

    @Override
    void act(HookContext hookContext) {
        if(!arguments.files) {
            arguments.files = [arguments.file]
        }
        arguments.files.each { updateInFile(hookContext, it) }
    }
    
    private void updateInFile(HookContext hookContext, def potentialFile) {
        File file = FileLoader.asFile(potentialFile)

        String text = file.text

        String pattern = arguments.pattern(hookContext.previousVersion, hookContext.position)
        String replacement = arguments.replacement(hookContext.currentVersion, hookContext.position)

        hookContext.logger.info("Replacing pattern \"$pattern\" with \"$replacement\" in $file.path")
        String replacedText = text.replaceAll(pattern, replacement)

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
