package henv.lint.values;

import org.yaml.snakeyaml.nodes.ScalarNode;

import java.nio.file.Path;
import java.util.Objects;

public class CustomScalarNode {
    private final ScalarNode scalarNode;
    private final Path yaml;

    public CustomScalarNode(ScalarNode scalarNode, Path yaml) {
        this.scalarNode = scalarNode;
        this.yaml = yaml;
    }

    public String getValue()
    {
        return scalarNode.getValue();
    }
    public int getLine()
    {
        return scalarNode.getStartMark().getLine();
    }

    public Path getYaml() {
        return yaml;
    }

    @Override
    public boolean equals(final Object other) {
        if(other == null)
        {
            return false;
        }
        if(this == other)
        {
            return true;
        }
        if(this.getClass() != other.getClass())
        {
            return false;
        }
        final CustomScalarNode node = (CustomScalarNode) other;

        return  Objects.equals(this.getValue(), node.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
