package pl.allegro.tech.build.axion.release.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class FileLoader {

    private static File root;

    public static void setRoot(File file) {
        FileLoader.root = file;
    }

    public static String readFrom(Object file) {
        File readableFile = asFile(file);

        try (FileInputStream fis = new FileInputStream(readableFile); StringWriter writer = new StringWriter()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

            for (int c = reader.read(); c >= 0; c = reader.read()) {
                writer.write(c);
            }

            return writer.toString();
        } catch (IOException e) {
            throw new FileOperationException(e);
        }
    }

    public static File asFile(Object file) {
        if (file instanceof File) {
            return ((File) (file));
        } else {
            return fileFromStringPath(file.toString());
        }

    }

    private static File fileFromStringPath(String string) {
        // fixes using release.customKey property on windows
        // 'C:/path/to/keyFile.ppk' is not recognized as absolute path
        File path = new File(string);

        if (!path.isAbsolute() && root != null) {
            return new File(root, string);
        }

        return path;
    }

    public static class FileOperationException extends RuntimeException {
        FileOperationException(Exception cause) {
            super(cause);
        }
    }
}
