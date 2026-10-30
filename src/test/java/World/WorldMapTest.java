package World;

import World.Biome.Biome;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorldMapTest {

    private static final long SEED = 12345;
    private static final TerrainGenerator GENERATOR = new TerrainGenerator(SEED);
    private static final WorldMap MAP = new WorldMap(GENERATOR);

    private static int topBlock(byte[][][] blocks, int x, int z) {
        for (int y = Chunk.CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z] != 0) return blocks[x][y][z];
        }
        return 0;
    }

    // True when pixel is base scaled by one shared brightness factor, give or take rounding
    private static boolean isShadeOf(int pixel, int base) {
        int[] p = {(pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF};
        int[] b = {(base >> 16) & 0xFF, (base >> 8) & 0xFF, base & 0xFF};
        int brightest = b[0] >= b[1] && b[0] >= b[2] ? 0 : b[1] >= b[2] ? 1 : 2;
        double light = (double) p[brightest] / b[brightest];
        for (int c = 0; c < 3; c++) {
            if (Math.abs(p[c] - b[c] * light) > 2.5) return false;
        }
        return true;
    }

    @Test
    void imageHasRequestedSize() {
        BufferedImage image = MAP.render(0, 0, 40, 24, 3, WorldMap.Mode.TERRAIN);

        assertEquals(40, image.getWidth());
        assertEquals(24, image.getHeight());
    }

    @Test
    void renderAreaCompressesBlocksIntoPixels() {
        assertEquals(1024, WorldMap.pixelsFor(4096, 4));
        assertEquals(4096, WorldMap.pixelsFor(4096, 1));
        BufferedImage image = MAP.renderArea(0, 0, 400, 100, 8, WorldMap.Mode.TERRAIN);

        assertEquals(50, image.getWidth());
        assertEquals(13, image.getHeight());
    }

    @Test
    void compressedPixelsMatchTheFullDetailMapAtTheSameColumn() {
        BufferedImage full = MAP.renderArea(0, 0, 256, 256, 1, WorldMap.Mode.BIOMES);
        BufferedImage compressed = MAP.renderArea(0, 0, 256, 256, 4, WorldMap.Mode.BIOMES);

        for (int i = 0; i < 64; i++) {
            for (int k = 0; k < 64; k++) {
                // A compressed pixel samples the column in the middle of its 4 x 4 patch
                int fullBiome = full.getRGB(i * 4 + 2, k * 4 + 2) & 0xFFFFFF;
                int compressedBiome = compressed.getRGB(i, k) & 0xFFFFFF;
                assertTrue(sameBiomeColour(fullBiome, compressedBiome),
                        String.format("pixel %d,%d is %06X compressed but %06X at full detail", i, k, compressedBiome, fullBiome));
            }
        }
    }

    // Shading differs between scales, so compare which biome colour each pixel is a shade of
    private static boolean sameBiomeColour(int a, int b) {
        for (Biome biome : GENERATOR.getBiomes()) {
            int colour = MAP.biomeColour(biome);
            if (isShadeOf(a, colour)) return isShadeOf(b, colour);
        }
        return false;
    }

    @Test
    void rejectsNonPositiveSizes() {
        assertThrows(IllegalArgumentException.class, () -> MAP.render(0, 0, 0, 10, 1, WorldMap.Mode.TERRAIN));
        assertThrows(IllegalArgumentException.class, () -> MAP.render(0, 0, 10, 10, 0, WorldMap.Mode.BIOMES));
        assertThrows(IllegalArgumentException.class, () -> MAP.renderArea(0, 0, 100, 100, 0, WorldMap.Mode.BIOMES));
    }

    @Test
    void sameSeedRendersIdenticalMaps() {
        BufferedImage a = MAP.render(300, -200, 64, 64, 8, WorldMap.Mode.TERRAIN);
        BufferedImage b = new WorldMap(new TerrainGenerator(SEED)).render(300, -200, 64, 64, 8, WorldMap.Mode.TERRAIN);

        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                assertEquals(a.getRGB(x, z), b.getRGB(x, z));
            }
        }
    }

    @Test
    void terrainPixelsShowTheTopBlockOfGeneratedChunks() {
        Set<Integer> seenBlocks = new HashSet<>();
        for (int cx = -10; cx <= 10; cx += 4) {
            for (int cz = -10; cz <= 10; cz += 4) {
                byte[][][] blocks = GENERATOR.generateChunkTerrain(new Vector3i(cx, 0, cz));
                // Centring on the chunk at one block per pixel lines pixel (x, z) up with block (x, z)
                BufferedImage image = MAP.render(cx * Chunk.CHUNK_SIZE + 8, cz * Chunk.CHUNK_SIZE + 8,
                        Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE, 1, WorldMap.Mode.TERRAIN);

                for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                        Block top = Block.values()[topBlock(blocks, x, z)];
                        seenBlocks.add(top.ordinal());
                        int pixel = image.getRGB(x, z) & 0xFFFFFF;
                        assertTrue(isShadeOf(pixel, MAP.blockColour(top)),
                                String.format("pixel %06X at chunk %d,%d block %d,%d is not a shade of %s", pixel, cx, cz, x, z, top));
                    }
                }
            }
        }
        assertTrue(seenBlocks.size() >= 3, "test area only covered blocks " + seenBlocks);
    }

    @Test
    void biomePixelsShowTheGeneratorsBiome() {
        BufferedImage image = MAP.render(0, 0, 128, 128, 32, WorldMap.Mode.BIOMES);
        Set<Biome> seen = new HashSet<>();

        for (int i = 0; i < 128; i++) {
            for (int k = 0; k < 128; k++) {
                int x = -128 * 32 / 2 + i * 32 + 16;
                int z = -128 * 32 / 2 + k * 32 + 16;
                Biome biome = GENERATOR.getBiome(x, z);
                seen.add(biome);
                int pixel = image.getRGB(i, k) & 0xFFFFFF;
                assertTrue(isShadeOf(pixel, MAP.biomeColour(biome)),
                        String.format("pixel %06X at %d,%d is not a shade of %s", pixel, x, z, biome));
            }
        }
        assertTrue(seen.size() >= 3, "map only covered " + seen);
    }

    @Test
    void everyBiomeAndTerrainBlockHasItsOwnColour() {
        Set<Integer> biomeColours = new HashSet<>();
        for (Biome biome : GENERATOR.getBiomes()) {
            assertTrue(biomeColours.add(MAP.biomeColour(biome)), biome + " shares a colour");
        }
        for (Block block : new Block[]{Block.GRASS, Block.DIRT, Block.STONE, Block.SAND, Block.SNOW, Block.WATER}) {
            assertNotEquals(0xFF00FF, MAP.blockColour(block), block + " has no texture colour");
        }
    }
}
