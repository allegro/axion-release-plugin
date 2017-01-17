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
            return new File(adjustStringPath(file.toString()))
        }
    }

    private static String adjustStringPath(String string) {
        if (!string.startsWith(File.separator) && root != null) {
            return root.canonicalPath + File.separator + string
        }
        return string
    }

}
