package Core;

import static org.lwjgl.opengl.GL46.*;

public class ChunkMesh {

    public static final int VERTICES_PER_QUAD = 4;
    public static final int INDICES_PER_QUAD = 6;

    // Worst case: a 3D checkerboard exposes all 6 faces of half the blocks in a chunk
    public static final int MAX_QUADS = 16 * 256 * 16 / 2 * 6;

    private static int sharedIndexBuffer = 0;

    private final int VAO;
    private final int VBO;
    private final int indexCount;

    public ChunkMesh(int[] packedVertices, int quadCount) {
        indexCount = quadCount * INDICES_PER_QUAD;

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, packedVertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, getSharedIndexBuffer());

        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, Integer.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static int[] buildQuadIndices(int quadCount) {
        int[] indices = new int[quadCount * INDICES_PER_QUAD];
        for (int quad = 0; quad < quadCount; quad++) {
            int vertex = quad * VERTICES_PER_QUAD;
            int i = quad * INDICES_PER_QUAD;
            indices[i]     = vertex;
            indices[i + 1] = vertex + 1;
            indices[i + 2] = vertex + 2;
            indices[i + 3] = vertex + 2;
            indices[i + 4] = vertex + 3;
            indices[i + 5] = vertex;
        }
        return indices;
    }

    private static int getSharedIndexBuffer() {
        if (sharedIndexBuffer == 0) {
            sharedIndexBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sharedIndexBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buildQuadIndices(MAX_QUADS), GL_STATIC_DRAW);
        }
        return sharedIndexBuffer;
    }

    public void draw() {
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L);
    }

    public void cleanup() {
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
    }
}
