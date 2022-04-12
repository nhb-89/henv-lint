package henv.lint.service;

import henv.lint.utils.YamlFileUtils;
import henv.lint.values.Finding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectLinter {
    private final List<Path> directories;
    private final Path tempDirectory;

    public ProjectLinter(Path projectRoot)
    {
        this.directories = new ArrayList<>();
        tempDirectory = YamlFileUtils.copyProjectToTemp(projectRoot);
        loadDirectories(tempDirectory);
        clean();

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
            var yamlFiles = YamlFileUtils.findYamlFiles(directory);
            Linter linter = new Linter(yamlFiles);
            findings.addAll(linter.lint());
        }
        return findings;
    }

    public void clean()
    {
        try {
            var gotmplFiles = YamlFileUtils.findGotmpliles(tempDirectory);
            for(var file :  gotmplFiles)
            {
                GotmplSyntaxReplacer replace = new GotmplSyntaxReplacer();
                replace.matchAndReplace(file, file);
            }
        } catch (IOException e) {
            System.out.println("Could not replace gotmpl syntax");
            e.printStackTrace();
        }
    }
}
