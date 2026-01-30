package Core;

import org.joml.Math;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.stb.STBImage.*;
import org.joml.*;

public class Renderer {

    private int VAO;
    private int VBO;
    private int EBO;
    private int shaderProgram;
    private int fragmentShader;
    private int vertexShader;
    private int texture;
    private Matrix4f trans;

    private void LoadTexture(String fileName) {

        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        stbi_set_flip_vertically_on_load(true);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // load and generate the texture
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer imageBuffer = STBImage.stbi_load(
                   fileName,
                    width,
                    height,
                    channels,
                    4 // Force RGBA channels

            );

            if (imageBuffer == null) {
                throw new RuntimeException("Image load failed: " + STBImage.stbi_failure_reason());
            }

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width.get(0),
                    height.get(0),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    imageBuffer
            );
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(imageBuffer);
        }
    }

    public void init() {
        float[] vertices = {
                // pos(x,y,z)       color(r,g,b)      uv(u,v)
                0.5f,  0.5f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 1.0f,
                -0.5f,  0.5f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, 0.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f,
                0.5f, -0.5f, 0.0f,  1.0f, 1.0f, 1.0f,  1.0f, 0.0f
        };


        int[] indices = {
            0,1,2,
            2,3,0
        };

        vertexShader = ShaderLoader.createShader("/shaders/vertexShader.glsl", GL_VERTEX_SHADER);
        fragmentShader = ShaderLoader.createShader("/shaders/fragmentShader.glsl", GL_FRAGMENT_SHADER);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram,vertexShader);
        glAttachShader(shaderProgram,fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        EBO = glGenBuffers();
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);

        //VBO
        glBindBuffer(GL_ARRAY_BUFFER,VBO);
        glBufferData(GL_ARRAY_BUFFER,vertices,GL_STATIC_DRAW);

        //EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices,GL_STATIC_DRAW);

        //vertex attribute layout
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        LoadTexture("/Users/michalkassa/Desktop/VoxelEngine/src/main/resources/images/texture.jpg");

        trans = new Matrix4f().identity();
    }

    public void update(){
        glClearColor(0.0f,0.0f,0.0f,0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(shaderProgram);

        trans.rotate((float) Math.toRadians(0.5f), new Vector3f(0f, 1.0f, 1.0f));
        int transformLoc = glGetUniformLocation(shaderProgram,"transform");
        float[] arr = new float[16];
        trans.get(arr);
        glUniformMatrix4fv(transformLoc, true, arr);

        glBindTexture(GL_TEXTURE_2D, texture);
        glBindVertexArray(VAO);s
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glDrawArrays(GL_TRIANGLES,0,6);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);


    }

    public void cleanup(){

    }
}
