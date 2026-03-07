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

    public byte[][][] generateChunkTerrain(Vector3i chunkPosition){
        byte[][][] blocks = new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];

        Vector3i worldPosition = new Vector3i(chunkPosition.x * Chunk.CHUNK_SIZE, 0 ,chunkPosition.z * Chunk.CHUNK_SIZE);
        Random random = new Random(seed);
        for( int x = 0 ; x < Chunk.CHUNK_SIZE; x++){
            for (int z = 0 ; z< Chunk.CHUNK_SIZE; z++){
                int height = noise.getHeight(new Vector3i(worldPosition.x + x,0 ,worldPosition.z + z));

                for (int y = 0; y < height ; y++) {
                    if( y > random.nextInt(256-100)+100){
                        blocks[x][y][z] = (byte) Block.STONE.ordinal();
                    }else if (y == height - 1) {
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
