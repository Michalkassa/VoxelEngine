package World;

import org.joml.Random;
import org.joml.Vector2d;
import org.joml.Vector3i;


public class TerrainGenerator {
    PerlinNoise noise;
    private long seed;
    private final int MAX_HEIGHT = 256;
    private final int MIN_HEIGHT = 10;
    private final int STONE_THRESHOLD = 65;

    public TerrainGenerator(long seed){
        this.seed = seed;
        this.noise = new PerlinNoise(seed);
    }

    public long getSeed() {
        return seed;
    }

    public int getHeight(float noiseValue) {
        double normalizedNoise = Math.pow((noiseValue + 1.0f) / 2.0f, 3);
        return (int) (MIN_HEIGHT + normalizedNoise * (MAX_HEIGHT - MIN_HEIGHT));
    }

    public byte[][][] generateChunkTerrain(Vector3i chunkPosition) {
        byte[][][] blocks = new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];

        int worldX = chunkPosition.x * Chunk.CHUNK_SIZE;
        int worldZ = chunkPosition.z * Chunk.CHUNK_SIZE;

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int height = getHeight(noise.getOctaveNoise(new Vector2d(worldX + x, worldZ + z)));

                for (int y = 0; y < height; y++) {
                    if (y > STONE_THRESHOLD) {
                        blocks[x][y][z] = (byte) Block.STONE.ordinal();
                    } else if (y == height - 1) {
                        blocks[x][y][z] = (byte) Block.GRASS.ordinal();
                    } else {
                        blocks[x][y][z] = (byte) Block.DIRT.ordinal();
                    }
                }
            }
        }
        return blocks;
    }

}
