package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

// A cube map drawn behind the terrain, loaded from six square images in one folder
public class Skybox {

    // In OpenGL's cube map order: +X, -X, +Y, -Y, +Z, -Z
    public static final String[] FACE_NAMES = { "right", "left", "top", "bottom", "front", "back" };
    private static final String FACE_EXTENSION = ".png";

    private final int cubeMap;
    private final Shader shader;
    // The skybox is a single triangle covering the screen, so it needs no vertex data, but core OpenGL still needs a VAO bound
    private final int emptyVao;
    private final int inverse_view_projection;
    private final int sky;

    private final Matrix4f viewProjection = new Matrix4f();
    private final float[] matBuffer = new float[16];

    // Loads the six faces from the folder, or uses a plain colour when the folder has none of them
    public Skybox(String directory, Vector3f fallbackColour) {
        cubeMap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMap);

        if (hasAnyFace(directory)) {
            loadFaces(directory);
        } else {
            System.out.println("No skybox images in " + directory + ", using a plain sky colour");
            loadSolidColour(fallbackColour);
        }

        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        // Blends across face edges so the seams between images don't show
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        shader = new Shader("/shaders/skyboxVertexShader.glsl", "/shaders/skyboxFragmentShader.glsl");
        inverse_view_projection = glGetUniformLocation(shader.getShaderProgram(), "inverse_view_projection");
        sky = glGetUniformLocation(shader.getShaderProgram(), "sky");
        emptyVao = glGenVertexArrays();
    }

    private static boolean hasAnyFace(String directory) {
        for (String face : FACE_NAMES) {
            if (new File(directory, face + FACE_EXTENSION).isFile()) return true;
        }
        return false;
    }

    private static void loadFaces(String directory) {
        // Cube map faces are read top row first, unlike 2D textures
        stbi_set_flip_vertically_on_load(false);
        int size = -1;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            for (int i = 0; i < FACE_NAMES.length; i++) {
                String path = new File(directory, FACE_NAMES[i] + FACE_EXTENSION).getPath();
                ByteBuffer image = STBImage.stbi_load(path, width, height, channels, 4);
                if (image == null) {
                    throw new RuntimeException("Skybox face load failed: " + path + " (" + STBImage.stbi_failure_reason() + ")");
                }
                if (width.get(0) != height.get(0) || (size != -1 && width.get(0) != size)) {
                    STBImage.stbi_image_free(image);
                    throw new RuntimeException("Skybox faces must all be square and the same size: " + path);
                }
                size = width.get(0);

                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8, size, size, 0,
                        GL_RGBA, GL_UNSIGNED_BYTE, image);
                STBImage.stbi_image_free(image);
            }
        }
    }

    private static void loadSolidColour(Vector3f colour) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer pixel = stack.malloc(4);
            pixel.put((byte) Math.round(colour.x * 255)).put((byte) Math.round(colour.y * 255))
                    .put((byte) Math.round(colour.z * 255)).put((byte) 255).flip();
            for (int i = 0; i < FACE_NAMES.length; i++) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8, 1, 1, 0,
                        GL_RGBA, GL_UNSIGNED_BYTE, pixel);
            }
        }
    }

    // Draws the sky over the whole screen. Call it before the terrain, which then draws on top.
    public void render(Matrix4f projection, Matrix4f view, int textureUnit) {
        // Only the camera's rotation matters, so the sky never gets closer as the player moves
        viewProjection.set(view).setTranslation(0f, 0f, 0f).mulLocal(projection).invert();

        shader.bind();
        glUniformMatrix4fv(inverse_view_projection, false, viewProjection.get(matBuffer));
        glUniform1i(sky, textureUnit);
        bindTexture(textureUnit);

        glDisable(GL_DEPTH_TEST);
        glBindVertexArray(emptyVao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
    }

    public void bindTexture(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMap);
        glActiveTexture(GL_TEXTURE0);
    }

    public void cleanup() {
        glDeleteTextures(cubeMap);
        glDeleteVertexArrays(emptyVao);
        shader.cleanup();
    }
}
