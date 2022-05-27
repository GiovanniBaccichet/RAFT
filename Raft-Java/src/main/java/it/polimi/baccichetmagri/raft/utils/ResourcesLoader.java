package it.polimi.baccichetmagri.raft.utils;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class ResourcesLoader {

    private ResourcesLoader() {
        // The class can't be instantiated.
    }

    /**
     * Load a file from the resources folder.
     *
     * @param path the file path relative to the resource folder.
     * @return the file.
     */
    public static String load(String path) {
        InputStream inputStream = ResourcesLoader.class.getResourceAsStream(path);

        if (inputStream == null) {
            throw new NullPointerException();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Load a file from the resources folder and parse it with Gson.
     *
     * @param path the file path relative to the resource folder.
     * @param classOfT the class to parse.
     * @param <T>      the class to parse.
     * @return the parsed file.
     */
    public static <T> T loadJson(String path, Type classOfT) {
        String config = load (path);
        return new Gson().fromJson(config, classOfT);
    }

    public static void write(String path, String content) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.write(content);
        }
    }

}
