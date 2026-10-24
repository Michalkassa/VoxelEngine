package World;

import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkTest {

    private byte[][][] blocks;
    private Chunk chunk;

    @BeforeEach
    void setUp() {
        blocks = new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];
        blocks[0][0][0] = (byte) Block.STONE.ordinal();
        blocks[15][255][15] = (byte) Block.SNOW.ordinal();
        blocks[7][64][3] = (byte) Block.GRASS.ordinal();
        chunk = new Chunk(new Vector3i(2, 0, -3), new ChunkManager(0), blocks);
    }

    @Test
    void chunkDimensions() {
        assertEquals(16, Chunk.CHUNK_SIZE);
        assertEquals(256, Chunk.CHUNK_HEIGHT);
    }

    @Test
    void getChunkPositionReturnsConstructorPosition() {
        assertEquals(new Vector3i(2, 0, -3), chunk.getChunkPosition());
    }

    @Test
    void getBlockReadsLoadedBlocks() {
        assertEquals(Block.STONE.ordinal(), chunk.getBlock(0, 0, 0));
        assertEquals(Block.SNOW.ordinal(), chunk.getBlock(15, 255, 15));
        assertEquals(Block.GRASS.ordinal(), chunk.getBlock(7, 64, 3));
        assertEquals(Block.AIR.ordinal(), chunk.getBlock(8, 100, 8));
    }

    @Test
    void constructorCopiesInputBlocks() {
        blocks[1][2][3] = (byte) Block.SAND.ordinal();
        blocks[0][0][0] = (byte) Block.AIR.ordinal();

        assertEquals(Block.AIR.ordinal(), chunk.getBlock(1, 2, 3));
        assertEquals(Block.STONE.ordinal(), chunk.getBlock(0, 0, 0));
    }

    @Test
    void constructorAcceptsSmallerInputArrays() {
        Chunk small = new Chunk(new Vector3i(), null, new byte[][][]{{{(byte) Block.DIRT.ordinal()}}});

        assertEquals(Block.DIRT.ordinal(), small.getBlock(0, 0, 0));
        assertEquals(Block.AIR.ordinal(), small.getBlock(1, 0, 0));
    }

    @Test
    void onlySectionsContainingBlocksAreAllocated() {
        assertTrue(chunk.isSectionAllocated(0));
        assertTrue(chunk.isSectionAllocated(4));
        assertTrue(chunk.isSectionAllocated(15));
        for (int section : new int[]{1, 2, 3, 5, 10, 14}) {
            assertFalse(chunk.isSectionAllocated(section), "section " + section);
        }
    }

    @Test
    void setBlockWritesBlock() {
        chunk.setBlock(new Vector3i(4, 100, 9), (byte) Block.WOOD.ordinal());

        assertEquals(Block.WOOD.ordinal(), chunk.getBlock(4, 100, 9));
        assertTrue(chunk.isSectionAllocated(100 / Chunk.SECTION_HEIGHT));
    }

    @Test
    void setBlockToAirRemovesBlock() {
        chunk.setBlock(new Vector3i(7, 64, 3), (byte) Block.AIR.ordinal());

        assertEquals(Block.AIR.ordinal(), chunk.getBlock(7, 64, 3));
    }

    @Test
    void settingAirInEmptySectionDoesNotAllocateIt() {
        chunk.setBlock(new Vector3i(1, 40, 1), (byte) Block.AIR.ordinal());

        assertFalse(chunk.isSectionAllocated(40 / Chunk.SECTION_HEIGHT));
    }

    @Test
    void chunkHasNoMeshUntilBuilt() {
        assertFalse(chunk.hasMesh());
        assertFalse(chunk.hasVisibleMesh());
    }

    @Test
    void getBlockRejectsCoordinatesOutsideChunk() {
        assertThrows(RuntimeException.class, () -> chunk.getBlock(-1, 0, 0));
        assertThrows(RuntimeException.class, () -> chunk.getBlock(16, 0, 0));
        assertThrows(RuntimeException.class, () -> chunk.getBlock(0, -1, 0));
        assertThrows(RuntimeException.class, () -> chunk.getBlock(0, 256, 0));
        assertThrows(RuntimeException.class, () -> chunk.getBlock(0, 0, -1));
        assertThrows(RuntimeException.class, () -> chunk.getBlock(0, 0, 16));
    }

    @Test
    void setBlockRejectsCoordinatesOutsideChunk() {
        byte stone = (byte) Block.STONE.ordinal();

        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(-1, 0, 0), stone));
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(16, 0, 0), stone));
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(0, -1, 0), stone));
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(0, 256, 0), stone));
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(0, 0, -1), stone));
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(0, 0, 16), stone));
    }

    @Test
    void rejectedSetBlockLeavesBlocksUnchanged() {
        assertThrows(RuntimeException.class, () -> chunk.setBlock(new Vector3i(16, 0, 0), (byte) 1));

        assertEquals(Block.STONE.ordinal(), chunk.getBlock(0, 0, 0));
    }

    @Test
    void isOnBorderDetectsEdgeColumns() {
        assertTrue(chunk.isOnBorder(0, 5));
        assertTrue(chunk.isOnBorder(15, 5));
        assertTrue(chunk.isOnBorder(5, 0));
        assertTrue(chunk.isOnBorder(5, 15));
        assertTrue(chunk.isOnBorder(0, 0));
        assertFalse(chunk.isOnBorder(1, 1));
        assertFalse(chunk.isOnBorder(14, 14));
        assertFalse(chunk.isOnBorder(7, 8));
    }

    @Test
    void renderAndCleanupWithoutMeshDoNothing() {
        assertDoesNotThrow(() -> chunk.render());
        assertDoesNotThrow(() -> chunk.cleanup());
    }
}
