package henv.lint.values;

import java.nio.file.Path;
import java.util.Objects;

public class Finding {
    private int line;
    private String key;
    private Path own;
    private Path other;

    public Finding(int line, String key, Path own, Path other) {
        this.line = line;
        this.key = key;
        this.own = own;
        this.other = other;
    }

    public int getLine() {
        return line;
    }

    public String getKey() {
        return key;
    }

    public Path getPath() {
        return own;
    }

    public String getFilename()
    {
        return own.getFileName().toString();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Error: ")
                .append("The key \"")
                .append(getKey())
                .append("\" exists in file ")
                .append(getFilename())
                .append(" but not not in file")
                .append(other.getFileName())
                .toString();
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
        final Finding finding = (Finding) other;

        return  Objects.equals(this.getLine(), finding.getLine()) &&
                Objects.equals(this.getKey(), finding.getKey()) &&
                Objects.equals(this.getPath(), finding.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, key, own);
    }
}
