package World;

import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerrainGeneratorTest {

    private static final long SEED = 12345;
    private static final int AREA_CHUNKS = 24;

    private static final byte AIR = (byte) Block.AIR.ordinal();
    private static final byte GRASS = (byte) Block.GRASS.ordinal();
    private static final byte DIRT = (byte) Block.DIRT.ordinal();
    private static final byte STONE = (byte) Block.STONE.ordinal();
    private static final byte SAND = (byte) Block.SAND.ordinal();
    private static final byte SNOW = (byte) Block.SNOW.ordinal();

    private static TerrainGenerator generator;
    private static List<byte[][][]> area;

    @BeforeAll
    static void generateArea() {
        generator = new TerrainGenerator(SEED);
        area = new ArrayList<>();
        for (int cx = -AREA_CHUNKS / 2; cx < AREA_CHUNKS / 2; cx++) {
            for (int cz = -AREA_CHUNKS / 2; cz < AREA_CHUNKS / 2; cz++) {
                area.add(generator.generateChunkTerrain(new Vector3i(cx, 0, cz)));
            }
        }
    }

    private static int surfaceY(byte[][][] blocks, int x, int z) {
        for (int y = Chunk.CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z] != AIR) return y;
        }
        return -1;
    }

    @Test
    void getSeedReturnsConstructorSeed() {
        assertEquals(SEED, generator.getSeed());
    }

    @Test
    void chunkHasExpectedDimensions() {
        byte[][][] blocks = area.get(0);

        assertEquals(Chunk.CHUNK_SIZE, blocks.length);
        assertEquals(Chunk.CHUNK_HEIGHT, blocks[0].length);
        assertEquals(Chunk.CHUNK_SIZE, blocks[0][0].length);
    }

    @Test
    void sameSeedGeneratesIdenticalChunks() {
        TerrainGenerator other = new TerrainGenerator(SEED);
        Vector3i position = new Vector3i(3, 0, -7);

        assertArrayEquals(generator.generateChunkTerrain(position), other.generateChunkTerrain(position));
    }

    @Test
    void differentSeedsGenerateDifferentTerrain() {
        TerrainGenerator other = new TerrainGenerator(SEED + 1);
        int differences = 0;

        for (int x = 0; x < 64; x++) {
            if (generator.getHeight(x * 7, x * 13) != other.getHeight(x * 7, x * 13)) differences++;
        }

        assertTrue(differences > 32, "only " + differences + " of 64 heights differed");
    }

    @Test
    void heightsStayWithinWorldBounds() {
        for (int x = -500; x <= 500; x += 25) {
            for (int z = -500; z <= 500; z += 25) {
                int height = generator.getHeight(x, z);
                assertTrue(height >= 1 && height < Chunk.CHUNK_HEIGHT, "height " + height + " at " + x + "," + z);
            }
        }
    }

    @Test
    void columnTopMatchesGetHeight() {
        Vector3i position = new Vector3i(-2, 0, 5);
        byte[][][] blocks = generator.generateChunkTerrain(position);

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int expected = generator.getHeight(position.x * Chunk.CHUNK_SIZE + x, position.z * Chunk.CHUNK_SIZE + z);
                assertEquals(expected - 1, surfaceY(blocks, x, z));
            }
        }
    }

    @Test
    void columnsAreSolidFromBedrockToSurfaceWithAirAbove() {
        for (byte[][][] blocks : area) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    int top = surfaceY(blocks, x, z);
                    for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                        if (y <= top) assertNotEquals(AIR, blocks[x][y][z]);
                        else assertEquals(AIR, blocks[x][y][z]);
                    }
                }
            }
        }
    }

    @Test
    void onlyTerrainBlocksAreGenerated() {
        for (byte[][][] blocks : area) {
            for (byte[][] column : blocks) {
                for (byte[] row : column) {
                    for (byte block : row) {
                        assertTrue(block == AIR || block == GRASS || block == DIRT || block == STONE
                                || block == SAND || block == SNOW, "unexpected block " + block);
                    }
                }
            }
        }
    }

    // Height difference across a column's neighbours, the same measure the generator uses
    private static int interiorSlope(byte[][][] blocks, int x, int z) {
        return Math.max(
                Math.abs(surfaceY(blocks, x + 1, z) - surfaceY(blocks, x - 1, z)),
                Math.abs(surfaceY(blocks, x, z + 1) - surfaceY(blocks, x, z - 1)));
    }

    // Checks top block, then a solid filler layer between minFiller and maxFiller blocks, then stone to the bottom.
    // Flat columns must carry the full layer.
    private static void assertLayered(byte top, byte filler, int minFiller, int maxFiller) {
        int checked = 0;
        for (byte[][][] blocks : area) {
            for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
                for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
                    int surface = surfaceY(blocks, x, z);
                    if (blocks[x][surface][z] != top) continue;

                    int y = surface - 1;
                    while (y >= 0 && blocks[x][y][z] == filler) y--;
                    int fillerDepth = surface - 1 - y;

                    assertTrue(fillerDepth >= minFiller && fillerDepth <= maxFiller,
                            fillerDepth + " filler blocks under the top at y=" + surface);
                    if (interiorSlope(blocks, x, z) == 0) {
                        assertEquals(maxFiller, fillerDepth, "flat column at y=" + surface + " has thinned soil");
                    }
                    for (; y >= 0; y--) {
                        assertEquals(STONE, blocks[x][y][z]);
                    }
                    checked++;
                }
            }
        }
        assertTrue(checked > 0, "no columns topped with block " + top);
    }

    @Test
    void grassSitsOnDirtOverStone() {
        // Four blocks of soil counting the grass, thinning to two on steep ground
        assertLayered(GRASS, DIRT, 1, 3);
    }

    @Test
    void sandColumnsAreSandOverStone() {
        // Five blocks of sand counting the top, thinning to two on steep ground
        assertLayered(SAND, SAND, 1, 4);
    }

    @Test
    void snowOnlyAppearsHighUpAndSitsOnStone() {
        for (byte[][][] blocks : area) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    int top = surfaceY(blocks, x, z);
                    if (blocks[x][top][z] != SNOW) continue;
                    assertTrue(top >= 150, "snow at y=" + top);
                    int y = top;
                    while (blocks[x][y][z] == SNOW) y--;
                    assertTrue(top - y <= 4, "snow layer too deep: " + (top - y));
                    assertEquals(STONE, blocks[x][y][z]);
                }
            }
        }
    }

    @Test
    void nothingBelowSurfaceLayerExceptStone() {
        for (byte[][][] blocks : area) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    int top = surfaceY(blocks, x, z);
                    for (int y = 0; y < top - 5; y++) {
                        assertEquals(STONE, blocks[x][y][z]);
                    }
                }
            }
        }
    }

    @Test
    void adjacentChunksJoinWithoutCliffs() {
        Vector3i left = new Vector3i(4, 0, 4);
        byte[][][] a = generator.generateChunkTerrain(left);
        byte[][][] b = generator.generateChunkTerrain(new Vector3i(left.x + 1, 0, left.z));

        for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
            int edgeA = surfaceY(a, Chunk.CHUNK_SIZE - 1, z);
            int edgeB = surfaceY(b, 0, z);
            int inner = surfaceY(a, Chunk.CHUNK_SIZE - 2, z);
            assertTrue(Math.abs(edgeA - edgeB) <= Math.max(3, Math.abs(edgeA - inner) * 3),
                    "seam jump " + edgeA + " -> " + edgeB);
        }
    }

    @Test
    void mostTerrainIsLowRollingLand() {
        int total = 0, low = 0;
        for (byte[][][] blocks : area) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    int top = surfaceY(blocks, x, z);
                    total++;
                    if (top >= 40 && top < 90) low++;
                }
            }
        }

        assertTrue(low > total * 0.75, "only " + (100.0 * low / total) + "% of columns are low land");
    }

    @Test
    void snowIsRare() {
        int total = 0, snow = 0;
        for (byte[][][] blocks : area) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                    total++;
                    if (blocks[x][surfaceY(blocks, x, z)][z] == SNOW) snow++;
                }
            }
        }

        assertTrue(snow < total * 0.1, (100.0 * snow / total) + "% of columns are snow");
    }

    @Test
    void mountainsAndSnowExistSomewhereInLargeWorld() {
        int maxHeight = 0;
        for (int x = -4000; x <= 4000; x += 40) {
            for (int z = -4000; z <= 4000; z += 40) {
                maxHeight = Math.max(maxHeight, generator.getHeight(x, z));
            }
        }

        assertTrue(maxHeight > 165, "tallest peak only reached y=" + maxHeight);
    }
}
