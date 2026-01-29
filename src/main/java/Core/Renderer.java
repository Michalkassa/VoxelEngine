package Core;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {

    private ShaderLoader shaderLoader;
    private int VAO;
    private int VBO;
    private int shaderProgram;
    private int fragmentShader;
    private int vertexShader;

    private int createShader(String path, int type) {
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


    public void init() {
        shaderLoader = new ShaderLoader();

        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        };

        vertexShader = createShader("/shaders/vertexShader.glsl", GL_VERTEX_SHADER);
        fragmentShader = createShader("/shaders/fragmentShader.glsl", GL_FRAGMENT_SHADER);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram,vertexShader);
        glAttachShader(shaderProgram,fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER,VBO);
        glBufferData(GL_ARRAY_BUFFER,vertices,GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

    }

    public void update(){
        glClearColor(0.0f,0.0f,0.0f,0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(shaderProgram);
        glBindVertexArray(VAO);
        glDrawArrays(GL_TRIANGLES,0,3);
    }

    public void cleanup(){

    }
}
