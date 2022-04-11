package henv.lint.service;

import com.google.common.base.Preconditions;
import henv.lint.values.CustomScalarNode;
import henv.lint.values.Finding;
import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Linter {

    private final List<Finding> findings;
    private final List<Path> yamlFiles;

    public Linter(final List<Path> yamlFiles) {
        Preconditions.checkArgument(yamlFiles.size() > 0, "henv-lint needs at least one file to compare");

        this.yamlFiles = yamlFiles;
        this.findings = new ArrayList<>();
    }

    public List<Finding> lint(){

        var truthYaml = getTruthFile(yamlFiles);

        for(Path yamlFile: yamlFiles)
        {
            try {
                compare(truthYaml, yamlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.unmodifiableList(this.findings);
    }

    private void compare(Path truthYaml, Path yamlFile)  throws IOException
    {
        var truthNodes = readYaml(truthYaml);
        var nodes = readYaml(yamlFile);

        List<Node> at = StreamSupport.stream(truthNodes.spliterator(), false).collect(Collectors.toList());
        List<Node> bt = StreamSupport.stream(nodes.spliterator(), false).collect(Collectors.toList());

        Node nodeA = getFirstDocumentOrReturnEmpty(at);
        Node nodeB = getFirstDocumentOrReturnEmpty(bt);

        iterate((MappingNode) nodeA, (MappingNode) nodeB, truthYaml, yamlFile);
    }

    private void iterate(MappingNode mappingNodeA, MappingNode mappingNodeB, Path file, Path otherFile)
    {
        var expectedKeys = getCustomScalarNodes(mappingNodeA, file);
        var actualKeys = getCustomScalarNodes(mappingNodeB, otherFile);

        var difference = CollectionUtils.disjunction(expectedKeys, actualKeys);
        createFindings(difference);

        for(var tuple: mappingNodeA.getValue())
        {
            var optionalKey = getScalarNode(tuple);
            if(optionalKey.isEmpty())
            {
                return;
            }
            var key = optionalKey.get().getValue();
            var keyB = tryToGetKey(key, mappingNodeB);

            //Key also found in other file
            if(keyB.isPresent())
            {
                if(tuple.getValueNode() instanceof MappingNode mappingNodeKeyA && keyB.get().getValueNode() instanceof MappingNode mappingNodeKeyB)
                {
                    iterate(mappingNodeKeyA, mappingNodeKeyB, file, otherFile);
                }
            }
        }
    }

    private Optional<NodeTuple> tryToGetKey(String key, MappingNode mappingNode)
    {
        return mappingNode.getValue().stream()
                .filter(nt -> nt.getKeyNode() instanceof ScalarNode)
                .filter(nt -> ((ScalarNode) nt.getKeyNode()).getValue().equals(key))
                .findFirst();
    }

    private void createFindings(final Collection<CustomScalarNode> scalarNodes)
    {
        scalarNodes.stream()
                .map(d -> new Finding(d.getLine(), d.getValue(), d.getYaml()))
                .forEachOrdered( f -> findings.add(f));
    }

    private Set<CustomScalarNode> getCustomScalarNodes(MappingNode mappingNode, Path yaml)
    {
        return mappingNode.getValue().stream()
                .map(n -> n.getKeyNode())
                .filter(ScalarNode.class::isInstance)
                .map(n -> new CustomScalarNode((ScalarNode) n, yaml))
                .collect(Collectors.toSet());
    }

    private Optional<ScalarNode> getScalarNode(NodeTuple nodeTuple)
    {
        return Stream.of(nodeTuple.getKeyNode())
                .filter(ScalarNode.class::isInstance)
                .map(ScalarNode.class::cast)
                .findAny();
    }

    private Node getFirstDocumentOrReturnEmpty(List<Node> nodes)
    {
        return nodes.isEmpty() ? getEmptyNode() : nodes.get(0);
    }

    private static Iterable<Node> readYaml(Path yamlFile) throws IOException {
        var inStream = Files.newInputStream(yamlFile);
        return new Yaml().composeAll(new InputStreamReader(inStream));
    }

    private static Node getEmptyNode()
    {
        var mark = new Mark("Empty",0,0,0,new char[0],0);
        return new MappingNode( new Tag("Empty"), false, Collections.emptyList(), mark, mark, DumperOptions.FlowStyle.AUTO);
    }

    private static Path getTruthFile(List<Path> yamlFiles)
    {
        var develop = yamlFiles.stream().filter(f -> f.toString().toLowerCase().contains("development") || f.toString().toLowerCase().contains("develop")).findFirst();
        return  develop.isPresent() ? develop.get() : yamlFiles.get(0);
    }
}
