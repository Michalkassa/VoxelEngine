package Core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkMeshTest {

    @Test
    void quadIndicesFollowTwoTrianglePattern() {
        assertArrayEquals(new int[]{0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4}, ChunkMesh.buildQuadIndices(2));
    }

    @Test
    void noQuadsGiveNoIndices() {
        assertEquals(0, ChunkMesh.buildQuadIndices(0).length);
    }

    @Test
    void sharedBufferCoversWorstCaseChunk() {
        int[] indices = ChunkMesh.buildQuadIndices(ChunkMesh.MAX_QUADS);

        assertEquals(16 * 256 * 16 / 2 * 6, ChunkMesh.MAX_QUADS);
        assertEquals(ChunkMesh.MAX_QUADS * 6, indices.length);
        assertEquals(ChunkMesh.MAX_QUADS * 4 - 1, indices[indices.length - 2]);
    }
}
