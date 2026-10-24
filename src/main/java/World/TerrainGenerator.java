package World;

import org.joml.Vector2d;
import org.joml.Vector3i;


/*
 * Layered height-map terrain.
 *
 * The final height of a column is built from several independent noise fields:
 *  - continent : very broad swells so the base level drifts across the world
 *  - hills     : common small rolling hills, their strength varies by region
 *  - desert    : a region mask; inside it the ground is sand shaped into dunes
 *  - mountain  : a rare region mask; inside it ridged noise raises tall peaks
 *  - warp      : offsets the sample coordinates so nothing looks grid-aligned
 *
 * Surface blocks are then picked from height and local slope, so snow settles
 * on high peaks and bare stone shows on cliffs.
 */
public class TerrainGenerator {
    private final long seed;

    private final PerlinNoise continentNoise;
    private final PerlinNoise hillNoise;
    private final PerlinNoise hillStrengthNoise;
    private final PerlinNoise desertNoise;
    private final PerlinNoise duneNoise;
    private final PerlinNoise mountainMaskNoise;
    private final PerlinNoise mountainNoise;
    private final PerlinNoise warpNoiseX;
    private final PerlinNoise warpNoiseZ;
    private final PerlinNoise detailNoise;

    private static final int MIN_HEIGHT = 1;
    private static final int MAX_HEIGHT = Chunk.CHUNK_HEIGHT - 1;

    private static final int BASE_HEIGHT = 64;
    private static final double CONTINENT_AMPLITUDE = 14.0;
    private static final double HILL_AMPLITUDE = 12.0;
    private static final double DUNE_AMPLITUDE = 6.0;
    private static final double MOUNTAIN_BASE_LIFT = 30.0;
    private static final double MOUNTAIN_AMPLITUDE = 130.0;
    private static final double WARP_STRENGTH = 45.0;

    // Raw octave noise mostly sits within +-0.25, this stretches it to roughly [-1, 1]
    private static final double NOISE_NORMALIZE = 4.0;

    // Mask thresholds on normalized noise: ~10% of the world rises into mountains
    // (full height only in the top ~3%), ~20% of the world is desert
    private static final double MOUNTAIN_MASK_START = 0.5;
    private static final double MOUNTAIN_MASK_FULL = 0.9;
    private static final double DESERT_MASK_START = 0.2;
    private static final double DESERT_MASK_FULL = 0.45;

    private static final int ROCK_HEIGHT = 120;
    private static final int SNOW_LINE = 165;
    private static final double SNOW_LINE_JITTER = 10.0;
    private static final int STEEP_SLOPE = 4;
    private static final int CLIFF_SLOPE = 7;
    private static final int DIRT_DEPTH = 4;
    private static final int SAND_DEPTH = 5;

    public TerrainGenerator(long seed){
        this.seed = seed;
        this.continentNoise    = new PerlinNoise(seed);
        this.hillNoise         = new PerlinNoise(seed + 1);
        this.hillStrengthNoise = new PerlinNoise(seed + 2);
        this.desertNoise       = new PerlinNoise(seed + 3);
        this.duneNoise         = new PerlinNoise(seed + 4);
        this.mountainMaskNoise = new PerlinNoise(seed + 5);
        this.mountainNoise     = new PerlinNoise(seed + 6);
        this.warpNoiseX        = new PerlinNoise(seed + 7);
        this.warpNoiseZ        = new PerlinNoise(seed + 8);
        this.detailNoise       = new PerlinNoise(seed + 9);
    }

    public long getSeed() {
        return seed;
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    private static double fbm(PerlinNoise noise, Vector2d p, double scale, int octaves) {
        double value = noise.getOctaveNoise(p, scale, octaves, 0.5, 2.0) * NOISE_NORMALIZE;
        return Math.max(-1.0, Math.min(1.0, value));
    }

    private double desertFactor(Vector2d p, double mountainFactor) {
        double desert = fbm(desertNoise, p, 0.0018, 3);
        return smoothstep(DESERT_MASK_START, DESERT_MASK_FULL, desert) * (1.0 - mountainFactor);
    }

    private double mountainFactor(Vector2d p) {
        double mask = fbm(mountainMaskNoise, p, 0.0012, 3);
        return smoothstep(MOUNTAIN_MASK_START, MOUNTAIN_MASK_FULL, mask);
    }

    public int getHeight(int x, int z) {
        Vector2d raw = new Vector2d(x, z);
        Vector2d p = new Vector2d(
                x + fbm(warpNoiseX, raw, 0.004, 2) * WARP_STRENGTH,
                z + fbm(warpNoiseZ, raw, 0.004, 2) * WARP_STRENGTH
        );

        double height = BASE_HEIGHT + fbm(continentNoise, p, 0.0015, 4) * CONTINENT_AMPLITUDE;

        double mountain = mountainFactor(p);
        double desert = desertFactor(p, mountain);

        // Small rolling hills everywhere, flatter in some regions and dampened in deserts
        double hillStrength = 0.35 + 0.65 * smoothstep(-0.25, 0.25, fbm(hillStrengthNoise, p, 0.003, 2));
        double hills = fbm(hillNoise, p, 0.012, 4) * HILL_AMPLITUDE * hillStrength;
        height += hills * (1.0 - 0.7 * desert);

        // Dunes: stretched ridged noise gives long wind-blown crests
        Vector2d duneP = new Vector2d(p.x * 0.6 + p.y * 0.2, p.y * 1.4);
        double dunes = duneNoise.getRidgedNoise(duneP, 0.025, 2, 0.4, 2.0) * DUNE_AMPLITUDE;
        height += dunes * desert;

        // Rare mountains: ridged peaks fading in from the mask edge
        if (mountain > 0.0) {
            // Sharpen the ridges so only the tallest points poke above the snow line
            double ridges = Math.pow(mountainNoise.getRidgedNoise(p, 0.006, 5, 0.5, 2.1), 1.6);
            height += mountain * (MOUNTAIN_BASE_LIFT + ridges * MOUNTAIN_AMPLITUDE * mountain);
        }

        return (int) Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, Math.round(height)));
    }

    public record Column(int height, Block surface, int surfaceDepth) {
        public Block blockAtDepth(int depth) {
            if (depth == 0) return surface;
            if (depth < surfaceDepth) return surface == Block.GRASS ? Block.DIRT : surface;
            return Block.STONE;
        }
    }

    public Column describeColumn(int worldX, int worldZ, int height, int slope) {
        Vector2d p = new Vector2d(worldX, worldZ);
        double desert = desertFactor(p, mountainFactor(p));
        double detail = fbm(detailNoise, p, 0.02, 2);
        int snowLine = (int) (SNOW_LINE + detail * SNOW_LINE_JITTER);

        if (height >= snowLine && slope < CLIFF_SLOPE) {
            return new Column(height, Block.SNOW, Math.min(4, 1 + (height - snowLine) / 10));
        }
        if (height >= ROCK_HEIGHT || slope >= STEEP_SLOPE) {
            return new Column(height, Block.STONE, 0);
        }
        if (desert > 0.5 + detail * 0.3) {
            return new Column(height, Block.SAND, SAND_DEPTH);
        }
        return new Column(height, Block.GRASS, DIRT_DEPTH);
    }

    public byte[][][] generateChunkTerrain(Vector3i chunkPosition) {
        byte[][][] blocks = new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];

        int worldX = chunkPosition.x * Chunk.CHUNK_SIZE;
        int worldZ = chunkPosition.z * Chunk.CHUNK_SIZE;

        // Height map with a one block border so slopes can be measured at chunk edges
        int[][] heights = new int[Chunk.CHUNK_SIZE + 2][Chunk.CHUNK_SIZE + 2];
        for (int x = 0; x < Chunk.CHUNK_SIZE + 2; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE + 2; z++) {
                heights[x][z] = getHeight(worldX + x - 1, worldZ + z - 1);
            }
        }

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int height = heights[x + 1][z + 1];
                int slope = Math.max(
                        Math.abs(heights[x + 2][z + 1] - heights[x][z + 1]),
                        Math.abs(heights[x + 1][z + 2] - heights[x + 1][z])
                );

                Column column = describeColumn(worldX + x, worldZ + z, height, slope);

                for (int y = 0; y < height; y++) {
                    blocks[x][y][z] = (byte) column.blockAtDepth(height - 1 - y).ordinal();
                }
            }
        }
        return blocks;
    }

}
