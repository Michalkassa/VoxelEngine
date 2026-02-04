package Core;

import World.Chunk;
import org.joml.Math;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.stb.STBImage.*;
import org.joml.*;

public class Renderer {

    private Shader shader;
    private Texture texture;
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f projection;
    private Camera camera;
    private Chunk chunk;


    private int model_transform;
    private int view_transform ;
    private int  projection_transform;

    private final float[] matBuffer = new float[16];


    public void init() {

        camera = new Camera(new Vector3f(0,1,0), new Vector3f(0,0,0));
        shader = new Shader("/shaders/vertexShader.glsl","/shaders/fragmentShader.glsl");
        texture = new Texture("/Users/michalkassa/Desktop/VoxelEngine/src/main/resources/images/texture.jpg");

        model = new Matrix4f().identity();
        view = new Matrix4f().identity();
        projection = new Matrix4f().identity();
        chunk = new Chunk(new Vector3f(0,0,0));


        model_transform = glGetUniformLocation(shader.getShaderProgram(), "model_transform");
        view_transform = glGetUniformLocation(shader.getShaderProgram(), "view_transform");
        projection_transform = glGetUniformLocation(shader.getShaderProgram(), "projection_transform");

        projection.perspective((float)Math.toRadians(45f), 1f, 0.1f, 128f);

        glEnable(GL_DEPTH_TEST);
    }

    public void update(float dt){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.update(dt);
        shader.bind();

        camera.getViewMatrix(view);

        //model.rotate(Math.toRadians(-55f) * deltaTime , new Vector3f(0f,1f,0f)).normalize3x3();

        glUniformMatrix4fv(model_transform, false, model.get(matBuffer));
        glUniformMatrix4fv(view_transform, false, view.get(matBuffer));
        glUniformMatrix4fv(projection_transform, false, projection.get(matBuffer));

        chunk.render();
        texture.bind();

    }

    public void cleanup(){
        texture.cleanup();
        chunk.cleanup();
        shader.cleanup();
    }
}
