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

    private Shader shader;
    private Texture texture;
    private Mesh mesh;
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f projection;


    public void init() {
        float[] vertices = {
                // Back face (z = -0.5)
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f,   // BR
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,    // TR
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,    // TR
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,   // TL
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL

                // Front face (z = 0.5)
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,   // BL
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,    // BR
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,    // TL
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,   // BL

                // Left face (x = -0.5)
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f,   // TL
                -0.5f, 0.5f, 0.5f, 1.0f, 1.0f,    // TR
                -0.5f, 0.5f, 0.5f, 1.0f, 1.0f,    // TR
                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f,   // BR
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL

                // Right face (x = 0.5)
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f,   // BL
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f,    // TL
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                0.5f, -0.5f, 0.5f, 0.0f, 1.0f,    // BR
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f,   // BL

                // Bottom face (y = -0.5)
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f,   // BR
                0.5f, -0.5f, 0.5f, 1.0f, 1.0f,    // TR
                0.5f, -0.5f, 0.5f, 1.0f, 1.0f,    // TR
                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f,   // TL
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,  // BL

                // Top face (y = 0.5)
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f,   // BL
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f,    // BR
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,     // TR
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,    // TL
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f    // BL

        };


        shader = new Shader("/shaders/vertexShader.glsl","/shaders/fragmentShader.glsl");
        mesh = new Mesh(vertices, 36);
        texture = new Texture("/Users/michalkassa/Desktop/VoxelEngine/src/main/resources/images/texture.jpg");

        model = new Matrix4f().identity();
        view = new Matrix4f().identity();
        projection = new Matrix4f().identity();



        model.rotate(Math.toRadians(30) , new Vector3f(1f,0f,0f)).normalize3x3();
        view.translate(new Vector3f(0f,0f,-4));
        projection.perspective((float)Math.toRadians(45f), 1f, 0.1f, 128f);

        glEnable(GL_DEPTH_TEST);
    }

    public void update(float deltaTime){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();

        model.rotate(Math.toRadians(-55f) * deltaTime , new Vector3f(0f,1f,0f)).normalize3x3();

        int model_transform = glGetUniformLocation(shader.getShaderProgram(),"model_transform");
        int view_transform = glGetUniformLocation(shader.getShaderProgram(),"view_transform");
        int projection_transform = glGetUniformLocation(shader.getShaderProgram(),"projection_transform");

        float[] modelarr = new float[16];
        model.get(modelarr);
        float[] viewarr = new float[16];
        view.get(viewarr);
        float[] projectionarr = new float[16];
        projection.get(projectionarr);


        glUniformMatrix4fv(model_transform, false, modelarr);
        glUniformMatrix4fv(view_transform, false, viewarr);
        glUniformMatrix4fv(projection_transform, false, projectionarr);


        texture.bind();
        mesh.draw();

    }

    public void cleanup(){
        texture.cleanup();
        mesh.cleanup();
        shader.cleanup();
    }
}
