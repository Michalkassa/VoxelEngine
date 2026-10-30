package World;

import World.Biome.Biome;
import World.Biome.Biomes;
import World.Biome.Climate;
import World.Biome.NoiseLayer;
import World.Biome.TerrainNoise;
import org.joml.Vector2d;
import org.joml.Vector3i;

import java.util.List;


/*
 * Biome-driven height-map terrain.
 *
 * Every column goes through the same steps:
 *  - warp    : the sample coordinates are offset so nothing looks grid-aligned
 *  - climate : broad temperature, humidity and elevation fields give the column a Climate
 *  - biomes  : biomes whose preferred climate is close enough each shape their own height,
 *              and the heights are blended by how close each biome is, so borders stay smooth
 *  - surface : the nearest biome picks the column's blocks from its height and slope
 *  - water   : any column whose ground is below SEA_LEVEL is filled with water up to it
 *
 * A broad continent swell is added on top of every biome so the base level drifts across the world.
 * How each biome looks lives in its own class under World.Biome.
 */
public class TerrainGenerator {
    private final long seed;
    private final List<Biome> biomes;
    private final TerrainNoise noise;

    private final PerlinNoise continentNoise;
    private final PerlinNoise warpNoiseX;
    private final PerlinNoise warpNoiseZ;
    private final PerlinNoise temperatureNoise;
    private final PerlinNoise humidityNoise;
    private final PerlinNoise elevationNoise;

    // Water fills every column up to just below this height
    public static final int SEA_LEVEL = 56;

    private static final int MIN_HEIGHT = 1;
    private static final int MAX_HEIGHT = Chunk.CHUNK_HEIGHT - 1;

    private static final double CONTINENT_AMPLITUDE = 14.0;
    private static final double WARP_STRENGTH = 45.0;

    // Smaller scale = larger biomes
    private static final double CLIMATE_SCALE = 0.0012;
    private static final int CLIMATE_OCTAVES = 3;

    // How much further than the nearest biome another biome may be and still blend in.
    // Larger values give wider, softer borders but cost more height samples per column.
    private static final double BLEND_RANGE = 0.3;

    // Wobbles the climate used for surface blocks so biome edges aren't smooth curves
    private static final double SURFACE_JITTER = 0.06;

    public TerrainGenerator(long seed) {
        this(seed, Biomes.ALL);
    }

    public TerrainGenerator(long seed, List<Biome> biomes) {
        if (biomes.isEmpty()) throw new IllegalArgumentException("terrain needs at least one biome");
        this.seed = seed;
        this.biomes = List.copyOf(biomes);
        this.noise = new TerrainNoise(seed);
        this.continentNoise   = new PerlinNoise(seed);
        this.warpNoiseX       = new PerlinNoise(seed + 7);
        this.warpNoiseZ       = new PerlinNoise(seed + 8);
        this.temperatureNoise = new PerlinNoise(seed + 10);
        this.humidityNoise    = new PerlinNoise(seed + 11);
        this.elevationNoise   = new PerlinNoise(seed + 12);
    }

    public long getSeed() {
        return seed;
    }

    public List<Biome> getBiomes() {
        return biomes;
    }

    private Vector2d warp(int x, int z) {
        Vector2d raw = new Vector2d(x, z);
        return new Vector2d(
                x + TerrainNoise.fbm(warpNoiseX, raw, 0.004, 2) * WARP_STRENGTH,
                z + TerrainNoise.fbm(warpNoiseZ, raw, 0.004, 2) * WARP_STRENGTH
        );
    }

    private Climate sampleClimate(Vector2d p) {
        return new Climate(
                TerrainNoise.fbm(temperatureNoise, p, CLIMATE_SCALE, CLIMATE_OCTAVES),
                TerrainNoise.fbm(humidityNoise, p, CLIMATE_SCALE, CLIMATE_OCTAVES),
                TerrainNoise.fbm(elevationNoise, p, CLIMATE_SCALE, CLIMATE_OCTAVES)
        );
    }

    public Climate getClimate(int x, int z) {
        return sampleClimate(warp(x, z));
    }

    private Biome nearestBiome(Climate climate) {
        Biome nearest = biomes.get(0);
        double nearestDistance = Double.MAX_VALUE;
        for (Biome biome : biomes) {
            double distance = biome.getClimate().distanceTo(climate);
            if (distance < nearestDistance) {
                nearest = biome;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public Biome getBiome(int x, int z) {
        return nearestBiome(getClimate(x, z));
    }

    public int getHeight(int x, int z) {
        Vector2d p = warp(x, z);
        Climate climate = sampleClimate(p);

        double[] distances = new double[biomes.size()];
        double nearest = Double.MAX_VALUE;
        for (int i = 0; i < distances.length; i++) {
            distances[i] = biomes.get(i).getClimate().distanceTo(climate);
            nearest = Math.min(nearest, distances[i]);
        }

        // Weights fall from 1 at the nearest biome to 0 at BLEND_RANGE further away,
        // so they change continuously as the climate drifts and borders never step
        double blended = 0.0;
        double totalWeight = 0.0;
        for (int i = 0; i < distances.length; i++) {
            double t = 1.0 - (distances[i] - nearest) / BLEND_RANGE;
            if (t <= 0.0) continue;
            double weight = t * t;
            blended += weight * biomes.get(i).height(p, noise);
            totalWeight += weight;
        }

        double height = blended / totalWeight + TerrainNoise.fbm(continentNoise, p, 0.0015, 4) * CONTINENT_AMPLITUDE;
        return (int) Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, Math.round(height)));
    }

    // height is the ground height, water stands waterDepth blocks deep on top of it
    public record Column(int height, Block surface, Block filler, int surfaceDepth, int waterDepth) {
        public Column(int height, Block surface, Block filler, int surfaceDepth) {
            this(height, surface, filler, surfaceDepth, 0);
        }

        // Grass is held up by dirt, every other surface block fills its own layer
        public Column(int height, Block surface, int surfaceDepth) {
            this(height, surface, surface == Block.GRASS ? Block.DIRT : surface, surfaceDepth);
        }

        public Column withWaterDepth(int depth) {
            return new Column(height, surface, filler, surfaceDepth, depth);
        }

        // Height of the column's top including any water
        public int top() {
            return height + waterDepth;
        }

        // Depth is counted down from the top of the column, water included
        public Block blockAtDepth(int depth) {
            if (depth < waterDepth) return Block.WATER;
            depth -= waterDepth;
            if (depth == 0) return surface;
            if (depth < surfaceDepth) return filler;
            return Block.STONE;
        }
    }

    // Height of the top of a column including any water standing on its ground
    public int surfaceHeight(int groundHeight) {
        return Math.max(groundHeight, SEA_LEVEL);
    }

    public Column describeColumn(int worldX, int worldZ, int height, int slope) {
        Vector2d p = warp(worldX, worldZ);
        Climate climate = sampleClimate(p);
        double detail = noise.fbm(NoiseLayer.DETAIL, p, 0.02, 2);

        Climate jittered = new Climate(
                climate.temperature() + detail * SURFACE_JITTER,
                climate.humidity() - detail * SURFACE_JITTER,
                climate.elevation()
        );
        return nearestBiome(jittered).surface(worldX, worldZ, height, slope, detail, noise)
                .withWaterDepth(surfaceHeight(height) - height);
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

                int top = column.top();
                for (int y = 0; y < top; y++) {
                    blocks[x][y][z] = (byte) column.blockAtDepth(top - 1 - y).ordinal();
                }
            }
        }
        return blocks;
    }

}
