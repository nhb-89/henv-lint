package henv.lint.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import henv.lint.values.CustomScalarNode;
import henv.lint.values.Finding;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Linter {

    private final List<Finding> findings;

    public Linter() {
        this.findings = new ArrayList<>();
    }

    public List<Finding> lint(final List<Path> yamlFiles){
        if(yamlFiles.size() < 2)
        {
            System.out.println("Nothing to compare!");
            return Collections.emptyList();
        }

        var develop = yamlFiles.stream().filter(f -> f.toString().toLowerCase().contains("development") || f.toString().toLowerCase().contains("develop")).findFirst();

        Path truthYaml = develop.isPresent() ? develop.get() : yamlFiles.get(0);

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

        var a = (MappingNode) nodeA;
        var b = (MappingNode) nodeB;
        iterate(a,b);
    }

    private void iterate(MappingNode mappingNodeA, MappingNode mappingNodeB)
    {
        var expectedKeys = getCustomScalarNodes(mappingNodeA);
        var actualKeys = getCustomScalarNodes(mappingNodeB);

        var difference = Sets.difference(expectedKeys, actualKeys);
        createFindings(difference);

        for(var tuple: mappingNodeA.getValue())
        {
            var key = getScalarNode(tuple).getValue();
            var keyB = iassent(key, mappingNodeB);

            //Key also found in other file
            if(keyB.isPresent())
            {
                if(tuple.getValueNode() instanceof MappingNode)
                {
                    var mappingNodeKeyA = (MappingNode) tuple.getValueNode();
                    var mappingNodeKeyB = (MappingNode) keyB.get().getValueNode();
                    iterate(mappingNodeKeyA, mappingNodeKeyB);
                }
                else
                {
                    // ScalarNode which has no value
                }
            }
        }
    }

    private boolean isKeyPresent(String key, MappingNode mappingNode)
    {
        return mappingNode.getValue().stream()
                .map( nt -> nt.getKeyNode())
                .filter( k -> k instanceof ScalarNode)
                .map(s -> (ScalarNode) s)
                .anyMatch(s -> s.getValue().equals(key));
    }

    private Optional<NodeTuple> iassent(String key, MappingNode mappingNode)
    {
        return mappingNode.getValue().stream()
                .filter(nt -> nt.getKeyNode() instanceof ScalarNode)
                .filter(nt -> ((ScalarNode) nt.getKeyNode()).getValue().equals(key))
                .findFirst();
    }

    private List<Finding> compare(MappingNode a, MappingNode b, Path truthYaml, Path compareYaml)
    {
        Preconditions.checkArgument(a instanceof MappingNode, "Element not of type MappingNode");
        Preconditions.checkArgument(b instanceof MappingNode, "Element not of type MappingNode");

        List<Finding> findings = new ArrayList<>();

        var expectedKeys = a.getValue().stream()
                .map(n -> n.getKeyNode())
                .filter( v -> v instanceof ScalarNode)
                .map(v -> new CustomScalarNode((ScalarNode) v, truthYaml, compareYaml))
                .collect(Collectors.toSet());

        var actualKeys = b.getValue().stream()
                .map(n -> n.getKeyNode())
                .filter( v -> v instanceof ScalarNode)
                .map(v -> new CustomScalarNode((ScalarNode) v, compareYaml, truthYaml))
                .collect(Collectors.toSet());

        var difference = Sets.difference(expectedKeys, actualKeys);
        createFindings(difference);

        return findings;
    }

    private void createFindings(Sets.SetView<CustomScalarNode> scalarNodes)
    {
        findings.addAll(scalarNodes.stream().map( d -> new Finding(d.getLine(), d.getValue(), d.getOwnFilePath(), d.getCompareFilePath())).collect(Collectors.toList()));
    }

    private Set<CustomScalarNode> getCustomScalarNodes(MappingNode mappingNode)
    {
        return mappingNode.getValue().stream()
                .map(n -> n.getKeyNode())
                .filter( v -> v instanceof ScalarNode)
                .map(n -> new CustomScalarNode((ScalarNode) n))
                .collect(Collectors.toSet());
    }

    private ScalarNode getScalarNode(NodeTuple nodeTuple)
    {
        var keyNode = nodeTuple.getKeyNode();
        if( keyNode instanceof ScalarNode)
        {
            return (ScalarNode) keyNode;
        }
        else
        {
            throw new IllegalStateException("KeyNode should be of type 'ScalarNode' but is type " + keyNode.getClass());
        }
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
}
