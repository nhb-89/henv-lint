package henv.lint.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlFileUtils {

    public static final String[] fileExtensions = {".yaml", ".yml", ".yaml.gotmpl", ".yml.gotmpl"};

    private YamlFileUtils()
    {
    }

    public static List<Path> findFiles(Path path) throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(file -> isEndWith(file.toString().toLowerCase()))
                    .collect(Collectors.toUnmodifiableList());
        }
        return result;
    }

    private static boolean isEndWith(String file) {
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






