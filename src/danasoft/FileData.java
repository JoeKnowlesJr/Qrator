package danasoft;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class FileData {

//    private Scanner scanner;

    @Contract(pure = true)
    FileData() {}

    @NotNull
    List<String> readData(String path) throws IOException {
        List<String> retVal = new ArrayList<>();
        Path s = Paths.get(path);
        if (Files.exists(s)) {
            retVal = Files.readAllLines(s);
        }
        return retVal;
    }

//    void writeData(String s, String path) {
//        FileWriter fileWriter;
//        try {
//            fileWriter = new FileWriter(path);
//            fileWriter.write(s);
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    void writeData(@NotNull List<String> data, String path) throws IOException {
        FileWriter fileWriter;
        fileWriter = new FileWriter(path);
        for (String s : data) {
            fileWriter.write(s + "\n");
        }
        fileWriter.close();
    }

//    @Override
//    public void close() {
//        if (scanner != null) scanner.close();
//    }
}


































