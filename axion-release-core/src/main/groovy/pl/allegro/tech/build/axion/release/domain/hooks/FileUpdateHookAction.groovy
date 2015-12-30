package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.util.FileLoader

import java.util.regex.Pattern

class FileUpdateHookAction implements ReleaseHookAction {

    private final Map arguments

    FileUpdateHookAction(Map arguments) {
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

        String pattern = arguments.pattern(hookContext.previousVersion, hookContext)
        String replacement = arguments.replacement(hookContext.releaseVersion, hookContext)

        hookContext.logger.info("Replacing pattern \"$pattern\" with \"$replacement\" in $file.path")
        String replacedText = text.replaceAll(Pattern.compile(pattern, Pattern.MULTILINE), replacement)

        file.write(replacedText)
        hookContext.addCommitPattern(file.canonicalPath)
    }

    static final class Factory extends DefaultReleaseHookFactory {
        
        @Override
        ReleaseHookAction create(Map arguments) {
            return new FileUpdateHookAction(arguments)
        }
    }

}
