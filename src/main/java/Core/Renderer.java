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
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glPolygonOffset;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

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
    private int light_space;
    private int shadow_map;
    private int shadow_texel;

    // Shadows cover a 512 x 512 block square around the camera, at about 8 shadow map texels per block
    private static final float SHADOW_RADIUS = 256f;
    // Deep enough to catch the tallest mountains between the sun and anything inside the square
    private static final float SHADOW_DEPTH = SHADOW_RADIUS + Chunk.CHUNK_HEIGHT * 2f;
    private static final int SHADOW_RESOLUTION = 4096;
    private static final int SHADOW_MAP_UNIT = 1;

    private Shader shadowShader;
    private ShadowMap shadowMap;
    private final ShadowProjection shadowProjection = new ShadowProjection(SHADOW_RADIUS, SHADOW_DEPTH, SHADOW_RESOLUTION);
    private final Frustum shadowFrustum = new Frustum();
    private int shadow_chunk_offset;
    private int shadow_voxel_scale;
    private int shadow_light_space;

    private final float[] matBuffer = new float[16];
    private final int[] viewport = new int[4];
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
        light_space = glGetUniformLocation(shader.getShaderProgram(), "light_space");
        shadow_map = glGetUniformLocation(shader.getShaderProgram(), "shadow_map");
        shadow_texel = glGetUniformLocation(shader.getShaderProgram(), "shadow_texel");

        shader.bind();
        glUniform1i(shadow_map, SHADOW_MAP_UNIT);
        glUniform1f(shadow_texel, 1f / SHADOW_RESOLUTION);

        shadowShader = new Shader("/shaders/shadowVertexShader.glsl", "/shaders/shadowFragmentShader.glsl");
        shadow_chunk_offset = glGetUniformLocation(shadowShader.getShaderProgram(), "chunk_offset");
        shadow_voxel_scale = glGetUniformLocation(shadowShader.getShaderProgram(), "voxel_scale");
        shadow_light_space = glGetUniformLocation(shadowShader.getShaderProgram(), "light_space");
        shadowMap = new ShadowMap(SHADOW_RESOLUTION);

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
        camera.update(dt);
        world.getSun().getDirection(sunDirection);

        renderShadowMap();

        glClearColor(0.24f, 0.54f, 0.9f, 1.0f); // sky blue
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shader.bind();

        camera.getViewMatrix(view);

        glUniformMatrix4fv(view_transform, false, view.get(matBuffer));
        glUniformMatrix4fv(projection_transform, false, projection.get(matBuffer));
        glUniform3f(camera_position, camera.position.x, camera.position.y, camera.position.z);
        glUniform3f(sun_direction, sunDirection.x, sunDirection.y, sunDirection.z);
        glUniformMatrix4fv(light_space, false, shadowProjection.getLightSpace().get(matBuffer));

        shadowMap.bindTexture(SHADOW_MAP_UNIT);
        texture.bind();


        frustum.update(projection,view);
        world.render(frustum, camera.position, chunk_offset, voxel_scale);

    }

    // Draws the terrain around the camera from the sun's point of view, keeping only how far away each surface is
    private void renderShadowMap() {
        shadowProjection.update(sunDirection, camera.position);
        shadowFrustum.update(shadowProjection.getProjection(), shadowProjection.getView());

        glGetIntegerv(GL_VIEWPORT, viewport);
        shadowMap.bindForWriting();
        shadowShader.bind();
        glUniformMatrix4fv(shadow_light_space, false, shadowProjection.getLightSpace().get(matBuffer));

        // Terrain has no bottom faces, so both sides are drawn, and depths are pushed back a little
        // so lit surfaces don't shadow themselves
        glDisable(GL_CULL_FACE);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.5f, 2f);

        world.render(shadowFrustum, camera.position, shadow_chunk_offset, shadow_voxel_scale);

        glDisable(GL_POLYGON_OFFSET_FILL);
        glEnable(GL_CULL_FACE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    public void cleanup(){
        texture.cleanup();
        world.cleanup();
        shader.cleanup();
        shadowShader.cleanup();
        shadowMap.cleanup();
    }
}
