package it.polimi.baccichetmagri.raft.utils;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JsonFilesHandler {

    private static final Gson gson = new Gson();

    private JsonFilesHandler() {
        // The class can't be instantiated.
    }

    /**
     * Load a file from the resources folder and parse it with Gson.
     *
     * @param path the file path relative to the resource folder.
     * @param classOfT the class to parse.
     * @param <T>      the class to parse.
     * @return the parsed file.
     */
    public static <T> T read(String path, Type classOfT) throws IOException {
        String fileContent = Files.readString(Path.of(path));
        return gson.fromJson(fileContent, classOfT);
    }

    public static void write(String path, Object content) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.write(gson.toJson(content));
        }
    }
}
