package Core;

import org.joml.Math;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import org.joml.Vector3f;

import World.Chunk;
import World.World;

public class Renderer {

    private Shader shader;
    private Texture texture;
    private Matrix4f view;
    private Matrix4f projection;
    private Camera camera;
    private Frustum frustum;
    private World world;

    private final float PROJECTION_MAX_DISTANCE = 2048f;
    private int chunk_offset;
    private int voxel_scale;
    private int view_transform ;
    private int  projection_transform;
    private int camera_position;
    private int sun_direction;

    private final float[] matBuffer = new float[16];
    private final Vector3f sunDirection = new Vector3f();

    public Renderer(World world, Camera camera) {
        this.world = world;
        this.camera = camera;
    }


    public void init() {

        shader = new Shader("/shaders/vertexShader.glsl","/shaders/fragmentShader.glsl");
        texture = new Texture("src/main/resources/images/texture.png");

        view = new Matrix4f().identity();
        projection = new Matrix4f().identity();
        frustum = new Frustum();

        chunk_offset = glGetUniformLocation(shader.getShaderProgram(), "chunk_offset");
        voxel_scale = glGetUniformLocation(shader.getShaderProgram(), "voxel_scale");
        view_transform = glGetUniformLocation(shader.getShaderProgram(), "view_transform");
        projection_transform = glGetUniformLocation(shader.getShaderProgram(), "projection_transform");
        camera_position = glGetUniformLocation(shader.getShaderProgram(), "camera_position");
        sun_direction = glGetUniformLocation(shader.getShaderProgram(), "sun_direction");

        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);

        float aspect_ratio = (float) width[0] / height[0];

        float farPlane = Math.max(PROJECTION_MAX_DISTANCE, (world.getRenderDistance() + 2) * Chunk.CHUNK_SIZE * 1.5f);
        projection.perspective((float)Math.toRadians(camera.getFOV()), aspect_ratio, 0.1f, farPlane);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    public void update(float dt){
        glClearColor(0.24f, 0.54f, 0.9f, 1.0f); // sky blue
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        camera.update(dt);
        shader.bind();

        camera.getViewMatrix(view);

        glUniformMatrix4fv(view_transform, false, view.get(matBuffer));
        glUniformMatrix4fv(projection_transform, false, projection.get(matBuffer));
        glUniform3f(camera_position, camera.position.x, camera.position.y, camera.position.z);
        world.getSun().getDirection(sunDirection);
        glUniform3f(sun_direction, sunDirection.x, sunDirection.y, sunDirection.z);


        texture.bind();


        frustum.update(projection,view);
        world.render(frustum, camera.position, chunk_offset, voxel_scale);

    }

    public void cleanup(){
        texture.cleanup();
        world.cleanup();
        shader.cleanup();
    }
}
