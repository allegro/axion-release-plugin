package pl.allegro.tech.build.axion.release.util

final class FileLoader {

    static String readFrom(def file) {
        File readableFile = asFile(file)
        return readableFile.getText('UTF-8')
    }
    
    static File asFile(def file) {
        if (file instanceof File) {
            return file
        } else {
            return new File(file.toString())
        }
    }

}
