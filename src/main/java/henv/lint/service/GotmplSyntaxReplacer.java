package henv.lint.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class GotmplSyntaxReplacer {

    private final String substitution = "";

    public void matchAndReplace(Path inPath, Path outPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(inPath.toFile()));
            String line = "";
            while(( line = reader.readLine()) != null)
            {
                if(line.trim().startsWith("{{ $"))
                {
                    sb.append(" ");
                }
                else
                {
                    sb.append(line);
                }
                sb.append(System.getProperty("line.separator"));
            }
            reader.close();

            var result = sb.toString();
            Files.write(outPath, result.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}