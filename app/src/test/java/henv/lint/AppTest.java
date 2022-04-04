/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package henv.lint;

import henv.lint.service.Linter;
import henv.lint.utils.YamlFileUtils;
import henv.lint.values.Finding;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    public void appHasAGreeting() throws IOException {
        var rootDirectory = Paths.get("./src/test/resources/test1");

        var yamlFiles = YamlFileUtils.findFiles(rootDirectory);
        Linter lint = new Linter(yamlFiles);;
        var findings = lint.lint();

        var expectedFinding = new Finding(1,"Hendrick", Paths.get("./src/test/resources/test1/file.yaml"));
        var expectedFinding2 = new Finding(2,"Harry", Paths.get("./src/test/resources/test1/development.yaml"));
        var expectedFinding3 = new Finding(1,"Hendrik", Paths.get("./src/test/resources/test1/development.yaml"));

        assertTrue(findings.contains(expectedFinding));
        assertTrue(findings.contains(expectedFinding2));
        assertTrue(findings.contains(expectedFinding3));
        assertTrue(findings.size() == 3);
    }

    @Test
    public void appHasAGreeting2() throws IOException {
        var rootDirectory = Paths.get("./src/test/resources/test2");
        var yamlFiles = YamlFileUtils.findFiles(rootDirectory);

        Linter lint = new Linter(yamlFiles);
        var findings = lint.lint();

        var expectedFinding = new Finding(10,"k", Paths.get("./src/test/resources/test2/development.yaml"));
        var expectedFinding2 = new Finding(10,"kkkkkk", Paths.get("./src/test/resources/test2/file.yaml"));

        assertTrue(findings.contains(expectedFinding));
        assertTrue(findings.contains(expectedFinding2));

        assertTrue(findings.size() == 2);
    }

    @Test
    public void appHasAGreeting3() throws IOException {
        var rootDirectory = Paths.get("./src/test/resources/test3");
        var yamlFiles = YamlFileUtils.findFiles(rootDirectory);

        Linter lint = new Linter(yamlFiles);
        var findings = lint.lint();

        //var expectedFinding = new Finding(1,"Harry", Paths.get("./src/test/resources/test2/file.yaml"));
        assertTrue(findings.size() == 2);
    }

    @Test
    public void appHasAGreeting4() throws IOException {
        var rootDirectory = Paths.get("./src/test/resources/test4");
        var yamlFiles = YamlFileUtils.findFiles(rootDirectory);

        Linter lint = new Linter(yamlFiles);
        var findings = lint.lint();

        var expectedFinding = new Finding(15,"java", Paths.get("./src/test/resources/test1/file.yaml"));
        assertTrue(findings.contains(expectedFinding));
        assertTrue(findings.size() == 1);
    }
}
