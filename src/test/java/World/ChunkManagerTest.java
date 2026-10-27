package World;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChunkManagerTest {

    private static final long SEED = 42;

    private ChunkManager chunkManager;
    private TerrainGenerator generator;

    @BeforeEach
    void setUp() {
        chunkManager = new ChunkManager(SEED);
        generator = new TerrainGenerator(SEED);
    }

    @Test
    void nothingIsLoadedInitially() {
        assertTrue(chunkManager.getLoadedChunks().isEmpty());
        assertFalse(chunkManager.isChunkLoaded(new Vector3i(0, 0, 0)));
        assertNull(chunkManager.getChunk(new Vector3i(0, 0, 0)));
    }

    @Test
    void loadChunkAsyncLoadsChunk() {
        chunkManager.loadChunkAsync(new Vector3i(1, 0, 2));

        assertTrue(chunkManager.isChunkLoaded(new Vector3i(1, 0, 2)));
        assertEquals(new Vector3i(1, 0, 2), chunkManager.getChunk(new Vector3i(1, 0, 2)).getChunkPosition());
        assertEquals(Set.of(new Vector3i(1, 0, 2)), chunkManager.getLoadedChunks());
    }

    @Test
    void loadChunkAsyncDoesNotReplaceLoadedChunk() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        Chunk first = chunkManager.getChunk(new Vector3i(0, 0, 0));

        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        assertSame(first, chunkManager.getChunk(new Vector3i(0, 0, 0)));
        assertEquals(1, chunkManager.getLoadedChunks().size());
    }

    @Test
    void loadedChunkUsesSeededTerrain() {
        Vector3i position = new Vector3i(-1, 0, 3);
        byte[][][] expected = generator.generateChunkTerrain(position);

        chunkManager.loadChunkAsync(position);
        Chunk chunk = chunkManager.getChunk(position);

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    assertEquals(expected[x][y][z], chunk.getBlock(x, y, z));
                }
            }
        }
    }

    @Test
    void getLoadedChunksReturnsCopy() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        chunkManager.getLoadedChunks().clear();

        assertTrue(chunkManager.isChunkLoaded(new Vector3i(0, 0, 0)));
    }

    @Test
    void worldBlockQueriesMatchGeneratedTerrain() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        int height = generator.getHeight(5, 9);

        assertTrue(chunkManager.isBlockAt(new Vector3i(5, height - 1, 9)));
        assertFalse(chunkManager.isBlockAt(new Vector3i(5, height, 9)));
        assertEquals(generator.generateChunkTerrain(new Vector3i(0, 0, 0))[5][height - 1][9],
                chunkManager.getBlockAt(new Vector3i(5, height - 1, 9)));
        assertEquals(0, chunkManager.getBlockAt(new Vector3i(5, height, 9)));
    }

    @Test
    void negativeWorldCoordinatesMapIntoNegativeChunks() {
        chunkManager.loadChunkAsync(new Vector3i(-1, 0, -1));
        int height = generator.getHeight(-1, -16);

        assertTrue(chunkManager.isBlockAt(new Vector3i(-1, height - 1, -16)));
        assertFalse(chunkManager.isBlockAt(new Vector3i(-1, height, -16)));
        assertEquals(chunkManager.getChunk(new Vector3i(-1, 0, -1)).getBlock(15, height - 1, 0),
                chunkManager.getBlockAt(new Vector3i(-1, height - 1, -16)));
    }

    @Test
    void queriesInUnloadedChunksReportAir() {
        assertFalse(chunkManager.isBlockAt(new Vector3i(100, 10, 100)));
        assertEquals(0, chunkManager.getBlockAt(new Vector3i(100, 10, 100)));
    }

    @Test
    void queriesOutsideVerticalRangeReportAir() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        assertFalse(chunkManager.isBlockAt(new Vector3i(1, -1, 1)));
        assertFalse(chunkManager.isBlockAt(new Vector3i(1, Chunk.CHUNK_HEIGHT, 1)));
        assertEquals(0, chunkManager.getBlockAt(new Vector3i(1, -1, 1)));
        assertEquals(0, chunkManager.getBlockAt(new Vector3i(1, Chunk.CHUNK_HEIGHT, 1)));
    }

    @Test
    void setBlockAtThrowsForUnloadedChunk() {
        assertThrows(RuntimeException.class,
                () -> chunkManager.setBlockAt(new Vector3i(0, 10, 0), (byte) Block.STONE.ordinal()));
    }

    @Test
    void setBlockAtThrowsOutsideVerticalRange() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        assertThrows(RuntimeException.class,
                () -> chunkManager.setBlockAt(new Vector3i(0, -1, 0), (byte) Block.STONE.ordinal()));
        assertThrows(RuntimeException.class,
                () -> chunkManager.setBlockAt(new Vector3i(0, Chunk.CHUNK_HEIGHT, 0), (byte) Block.STONE.ordinal()));
    }

    @Test
    void getChunkByWorldPositionUsesContainingChunk() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(1, 0, -1));

        assertSame(chunkManager.getChunk(new Vector3i(0, 0, 0)), chunkManager.getChunk(new Vector3f(3.2f, 70, 15.4f)));
        assertSame(chunkManager.getChunk(new Vector3i(1, 0, -1)), chunkManager.getChunk(new Vector3f(16.1f, 70, -3f)));
    }

    @Test
    void getChunkByWorldPositionJustBelowZeroUsesNegativeChunk() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(-1, 0, 0));

        assertSame(chunkManager.getChunk(new Vector3i(-1, 0, 0)), chunkManager.getChunk(new Vector3f(-0.7f, 70, 5)));
    }

    @Test
    void unloadChunkRemovesChunk() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        chunkManager.unloadChunk(new Vector3i(0, 0, 0));

        assertFalse(chunkManager.isChunkLoaded(new Vector3i(0, 0, 0)));
        assertFalse(chunkManager.isBlockAt(new Vector3i(1, 1, 1)));
    }

    @Test
    void unloadingMissingChunkIsHarmless() {
        assertDoesNotThrow(() -> chunkManager.unloadChunk(new Vector3i(9, 0, 9)));
    }

    @Test
    void cleanupUnloadsEverything() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(1, 0, 0));

        chunkManager.cleanup();

        assertTrue(chunkManager.getLoadedChunks().isEmpty());
    }

    @Test
    void hasAllNeighboursRequiresSidesAndDiagonals() {
        Vector3i center = new Vector3i(0, 0, 0);
        chunkManager.loadChunkAsync(new Vector3i(1, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(-1, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 1));
        chunkManager.loadChunkAsync(new Vector3i(0, 0, -1));
        assertFalse(chunkManager.hasAllNeighbours(center));

        chunkManager.loadChunkAsync(new Vector3i(1, 0, 1));
        chunkManager.loadChunkAsync(new Vector3i(1, 0, -1));
        chunkManager.loadChunkAsync(new Vector3i(-1, 0, 1));
        assertFalse(chunkManager.hasAllNeighbours(center));

        chunkManager.loadChunkAsync(new Vector3i(-1, 0, -1));
        assertTrue(chunkManager.hasAllNeighbours(center));
    }

    @Test
    void asyncLoadedChunksAreNotMeshedImmediately() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));

        assertFalse(chunkManager.getChunk(new Vector3i(0, 0, 0)).hasMesh());
    }

    @Test
    void setBlockAtChangesBlockInUnmeshedChunk() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        chunkManager.loadChunkAsync(new Vector3i(-1, 0, 0));

        chunkManager.setBlockAt(new Vector3i(0, 200, 0), (byte) Block.WOOD.ordinal());
        chunkManager.setBlockAt(new Vector3i(-1, 200, 3), (byte) Block.LEAF.ordinal());

        assertEquals(Block.WOOD.ordinal(), chunkManager.getBlockAt(new Vector3i(0, 200, 0)));
        assertEquals(Block.LEAF.ordinal(), chunkManager.getBlockAt(new Vector3i(-1, 200, 3)));
    }

    @Test
    void setBlockAtCanRemoveTerrain() {
        chunkManager.loadChunkAsync(new Vector3i(0, 0, 0));
        int height = generator.getHeight(4, 4);

        chunkManager.setBlockAt(new Vector3i(4, height - 1, 4), (byte) Block.AIR.ordinal());

        assertFalse(chunkManager.isBlockAt(new Vector3i(4, height - 1, 4)));
    }
}
