package henv.lint.service;

import henv.lint.utils.YamlFileUtils;
import henv.lint.values.Finding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectLinter {
    private final List<Path> directories;

    public ProjectLinter(Path projectRoot)
    {
        this.directories = new ArrayList<>();
        loadDirectories(projectRoot);
    }

    private void loadDirectories(Path projectRoot)
    {
        try {
            directories.addAll(YamlFileUtils.findEnvironmentDirectories(projectRoot));
        } catch (IOException e) {
            System.out.println("Could not load environment directories");
            e.printStackTrace();
        }
    }

    public List<Finding> lintProject() throws IOException {
        List<Finding> findings = new ArrayList<>();
        for(var directory : directories)
        {
            var yamlFiles = YamlFileUtils.findFiles(directory);
            Linter linter = new Linter(yamlFiles);
            findings.addAll(linter.lint());
        }
        return findings;
    }

}
