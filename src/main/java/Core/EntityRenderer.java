package Core;

import Entity.Entity;
import Entity.EntityManager;
import Entity.NPC;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

// Draws NPCs as plain coloured boxes the size of their bounding box, lit by the sun
public class EntityRenderer {

    private static final Vector3f NPC_COLOUR = new Vector3f(0.85f, 0.35f, 0.25f);

    private Shader shader;
    private int vao;
    private int vbo;
    private int model;
    private int viewTransform;
    private int projectionTransform;
    private int sunDirection;
    private int colour;

    private final Matrix4f modelMatrix = new Matrix4f();
    private final float[] matBuffer = new float[16];

    public void init() {
        shader = new Shader("/shaders/entityVertexShader.glsl", "/shaders/entityFragmentShader.glsl");
        int program = shader.getShaderProgram();
        model = glGetUniformLocation(program, "model");
        viewTransform = glGetUniformLocation(program, "view_transform");
        projectionTransform = glGetUniformLocation(program, "projection_transform");
        sunDirection = glGetUniformLocation(program, "sun_direction");
        colour = glGetUniformLocation(program, "colour");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, BoxGeometry.vertices(), GL_STATIC_DRAW);
        int stride = BoxGeometry.FLOATS_PER_VERTEX * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render(EntityManager entities, Matrix4f view, Matrix4f projection, Vector3f sun) {
        shader.bind();
        glUniformMatrix4fv(viewTransform, false, view.get(matBuffer));
        glUniformMatrix4fv(projectionTransform, false, projection.get(matBuffer));
        glUniform3f(sunDirection, sun.x, sun.y, sun.z);
        glUniform3f(colour, NPC_COLOUR.x, NPC_COLOUR.y, NPC_COLOUR.z);

        glBindVertexArray(vao);
        for (Entity entity : entities.getEntities(NPC.class)) {
            modelMatrix.translation(entity.getPosition()).scale(entity.getSize());
            glUniformMatrix4fv(model, false, modelMatrix.get(matBuffer));
            glDrawArrays(GL_TRIANGLES, 0, BoxGeometry.VERTEX_COUNT);
        }
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        shader.cleanup();
    }
}
