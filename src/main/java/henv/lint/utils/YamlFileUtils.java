package henv.lint.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlFileUtils {

    public static final String[] fileExtensions = {".yaml", ".yml", ".yaml.gotmpl", ".yml.gotmpl"};
    public static int DIRECTORY_SEARCH_MAX_DEPTH = 2;

    private static Predicate<Path> isYamlFile = p -> hasYamlFileExtension(p.toString().toLowerCase());
    private static Predicate<Path> isEnvironmentsDirectory = p -> p.getFileName().toString().equals("environments");

    private YamlFileUtils()
    {
    }
    public static List<Path> findEnvironmentDirectories(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(Files::isDirectory)
                    .filter(isEnvironmentsDirectory)
                    .collect(Collectors.toUnmodifiableList());
        }
        return result;
    }

    public static List<Path> findFiles(Path path) throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path, DIRECTORY_SEARCH_MAX_DEPTH)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(isYamlFile)
                    .collect(Collectors.toUnmodifiableList());
        }
        return result;
    }

    private static boolean hasYamlFileExtension(String file) {
        boolean result = false;
        for (String fileExtension : fileExtensions) {
            if (file.endsWith(fileExtension)) {
                result = true;
                break;
            }
        }
        return result;
    }
}






