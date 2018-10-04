package pl.allegro.tech.build.axion.release.util

final class FileLoader {

    private static File root

    static void setRoot(File file) {
        this.root = file
    }

    static String readFrom(def file) {
        File readableFile = asFile(file)
        return readableFile.getText('UTF-8')
    }
    
    static String readIfFile(def potentialFile) {
        if(potentialFile instanceof File) {
            return readFrom(potentialFile)
        }
        return potentialFile
    }
    
    static File asFile(def file) {
        if (file instanceof File) {
            return file
        } else {
            return fileFromStringPath(file.toString())
        }
    }

    private static File fileFromStringPath(String string) {
        // fixes using release.customKey property on windows
        // 'C:/path/to/keyFile.ppk' is not recognized as absolute path

        // WAS:
        /*
        if (!string.startsWith(File.separator) && root != null) {
            return root.canonicalPath + File.separator + string
        }
        return string
        */
        
        // FIX:
        File path = new File(string)
        
        if (!path.isAbsolute() && root != null) {
            return new File(root, string)
        }
        return path
    }

}
