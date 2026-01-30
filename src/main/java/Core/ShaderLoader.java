package Core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

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

     public static int createShader(String path, int type) {
        String source = ShaderLoader.loadFromFile(path);
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            throw new RuntimeException("Failed to compile shader '" + path + "':\n" + log);
        }

        return shader;
    }


}
