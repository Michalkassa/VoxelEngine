package Core;

import Entity.TestEntity;
import World.Chunk;
import World.ChunkManager;
import org.joml.Math;
import org.lwjgl.glfw.GLFW;
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
    private ChunkManager chunkManager;
    private TestEntity test;


    private int model_transform;
    private int view_transform ;
    private int  projection_transform;

    private final float[] matBuffer = new float[16];


    public void init() {

        camera = new Camera(new Vector3f(0,2,0), new Vector3f(0,0,0));
        shader = new Shader("/shaders/vertexShader.glsl","/shaders/fragmentShader.glsl");
        texture = new Texture("/Users/michalkassa/Desktop/VoxelEngine/src/main/resources/images/texture.png");

        model = new Matrix4f().identity();
        view = new Matrix4f().identity();
        projection = new Matrix4f().identity();

        model_transform = glGetUniformLocation(shader.getShaderProgram(), "model_transform");
        view_transform = glGetUniformLocation(shader.getShaderProgram(), "view_transform");
        projection_transform = glGetUniformLocation(shader.getShaderProgram(), "projection_transform");

        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);

        float aspect_ratio = (float) width[0] / height[0];

        projection.perspective((float)Math.toRadians(80f), aspect_ratio, 0.1f, 512f);

        chunkManager = new ChunkManager();

        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.setBlockAt(new Vector3i(1,1,0),(byte) 0);
        test = new TestEntity(new Vector3f(1,10,1));

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    public void update(float dt){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.update(dt);
        shader.bind();

        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.unloadChunksOutOfRadius(camera.getChunkPosition(), 3);
        camera.getViewMatrix(view);

        //model.rotate(Math.toRadians(-55f) * deltaTime , new Vector3f(0f,1f,0f)).normalize3x3();

        glUniformMatrix4fv(model_transform, false, model.get(matBuffer));
        glUniformMatrix4fv(view_transform, false, view.get(matBuffer));
        glUniformMatrix4fv(projection_transform, false, projection.get(matBuffer));


        texture.bind();


        model.identity().translate(test.getPosition());
        glUniformMatrix4fv(model_transform, false, model.get(matBuffer));
        test.render();

        model.identity();
        glUniformMatrix4fv(model_transform, false, model.get(matBuffer));
        chunkManager.renderChunks();

        test.update(dt, chunkManager.getChunk(test.getPosition()));

    }



    public void cleanup(){
        texture.cleanup();
        chunkManager.cleanup();
        test.cleanup();
        shader.cleanup();
    }
}
