package danasoft;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

class Filename {
    private String fullPath;
    private static final char pathSeparator = '/';
    private static final char extensionSeparator = '.';

//    @Contract(pure = true)
//    Filename(){}

    @Contract(pure = true)
    Filename(String str) {
        fullPath = str;
    }

    boolean isValid() {
        return filename().length() > 1 && extension().length() > 1;
    }

    @NotNull
    String extension() {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    @NotNull
    private String filename() { // gets filename without extension
        int dot = fullPath.lastIndexOf(extensionSeparator);
        int sep = fullPath.lastIndexOf(pathSeparator);
        if (dot == -1 || sep == -1) return fullPath;
        return fullPath.substring(sep + 1, dot);
    }

    String getFullFilename() {
        return filename() + "." + extension();
    }

    String fullPath() {
        return fullPath;
    }
}
