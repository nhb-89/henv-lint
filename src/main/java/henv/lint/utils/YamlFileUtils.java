package henv.lint.utils;

import henv.lint.service.CopyFileVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlFileUtils {

    public static final String[] fileExtensions = {".yaml", ".yml", ".yaml.gotmpl", ".yml.gotmpl"};
    public static int DIRECTORY_SEARCH_MAX_DEPTH = 2;
    public static String tmpDirsLocation = System.getProperty("java.io.tmpdir");

    public static Predicate<Path> isYamlFile = p -> hasYamlFileExtension(p.toString().toLowerCase());
    public static Predicate<Path> isGotmplFile = p -> p.toString().toLowerCase().endsWith("gotmpl");
    public static Predicate<Path> isEnvironmentsDirectory = p -> p.getFileName().toString().equals("environments");

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

    public static List<Path> findYamlFiles(Path path) throws IOException {
        return findFiles(path, isYamlFile, DIRECTORY_SEARCH_MAX_DEPTH);
    }

    public static List<Path> findGotmpliles(Path path) throws IOException {
        return findFiles(path, isYamlFile, Integer.MAX_VALUE);
    }

    public static List<Path> findFiles(Path path, Predicate<Path> predicate, int maxDepth ) throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path, maxDepth)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(predicate)
                    .collect(Collectors.toUnmodifiableList());
        }
        return result;
    }

    public static boolean hasYamlFileExtension(String file) {
        boolean result = false;
        for (String fileExtension : fileExtensions) {
            if (file.endsWith(fileExtension)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static Path copyProjectToTemp(Path sourceDirectoryLocation)
    {
        Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
        try {
            Path tmpdir = Files.createDirectories(path);
            Files.walkFileTree(sourceDirectoryLocation, new CopyFileVisitor(tmpdir));
        } catch (IOException e) {
            System.out.println("Could not create temp directory");
            e.printStackTrace();
        }
        return path;
    }
}






