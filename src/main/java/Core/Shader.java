package Core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int SHADER_PROGRAM;

    public Shader(String vertexShaderPath, String fragmentShaderPath){

        int vertexShader = createShader(vertexShaderPath, GL_VERTEX_SHADER);
        int fragmentShader = createShader(fragmentShaderPath, GL_FRAGMENT_SHADER);
        SHADER_PROGRAM = glCreateProgram();
        glAttachShader(SHADER_PROGRAM,vertexShader);
        glAttachShader(SHADER_PROGRAM,fragmentShader);
        glLinkProgram(SHADER_PROGRAM);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void bind(){
        glUseProgram(SHADER_PROGRAM);
    }

    public void unbind(){
        glUseProgram(0);
    }

    public void cleanup(){
        glDeleteProgram(SHADER_PROGRAM);
    }

    public int getShaderProgram(){
        return SHADER_PROGRAM;
    }
    private static String loadFromFile(String path) {
        StringBuilder res = new StringBuilder();

        try (InputStream in = Shader.class.getResourceAsStream(path)) {

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

    private static int createShader(String path, int type) {
        String source = loadFromFile(path);
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
