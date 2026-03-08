package World;

import org.joml.Random;
import org.joml.Vector3i;


public class TerrainGenerator {
    RandomNoise noise;
    private long seed;

    public TerrainGenerator(long seed){
        this.seed = seed;
        this.noise = new RandomNoise(seed);
    }

    public long getSeed() {
        return seed;
    }

    public byte[][][] generateChunkTerrain(Vector3i chunkPosition) {
        byte[][][] blocks = new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];

        int worldX = chunkPosition.x * Chunk.CHUNK_SIZE;
        int worldZ = chunkPosition.z * Chunk.CHUNK_SIZE;

        int stoneThreshold = 50;

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                //TODO BIGGEST BOTTLE NECK
                int height = noise.getHeight(worldX + x, worldZ + z);

                for (int y = 0; y < height; y++) {
                    if (y > stoneThreshold) {
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
