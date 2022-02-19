/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package henv.lint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flipkart.zjsonpatch.JsonDiff;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        JsonNode file1 = objectMapper.readTree(new File("C:\\Users\\Nils Berger\\IdeaProjects\\henv-lint\\app\\src\\main\\java\\henv\\lint\\file1.yaml"));
        JsonNode file2 = objectMapper.readTree(new File("C:\\Users\\Nils Berger\\IdeaProjects\\henv-lint\\app\\src\\main\\java\\henv\\lint\\file2.yaml"));
        JsonNode patch = JsonDiff.asJson(file1, file2);
        for(Iterator<JsonNode> iter = patch.elements(); iter.hasNext(); ){
            JsonNode node = iter.next();
            int a = 0;


        }
        String diffs = patch.toString();

        System.out.println(diffs);
    }
}
