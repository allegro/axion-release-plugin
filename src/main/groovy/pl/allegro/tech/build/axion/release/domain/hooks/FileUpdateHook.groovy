package pl.allegro.tech.build.axion.release.domain.hooks

import pl.allegro.tech.build.axion.release.util.FileLoader

class FileUpdateHook implements ReleaseHook {
    
    @Override
    void act(HookContext hookContext, Map arguments, Closure customAction) {
        File file = FileLoader.asFile(arguments.file)
        
        String text = file.text
        String replacedText = text.replaceAll(arguments.pattern(hookContext), arguments.replacement(hookContext))
        
        file.write(replacedText)
        hookContext.addCommitPattern(file.canonicalPath)
    }
    
    
}
