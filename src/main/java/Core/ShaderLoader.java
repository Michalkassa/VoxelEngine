package Core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ShaderLoader {

    public static String loadFromFile(String path) {
        StringBuilder res = new StringBuilder();

        try (InputStream in = ShaderLoader.class.getResourceAsStream(path)) {

            if (in == null) {
                throw new RuntimeException("Shader not found: " + path);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line).append('\n');
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }

        return res.toString();
    }


}
