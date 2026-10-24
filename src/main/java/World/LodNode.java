package World;

import org.joml.Vector3i;

// A square of 2^level by 2^level chunks; level 0 is a single full-detail chunk
public record LodNode(int level, int x, int z) {

    public int sizeInChunks() {
        return 1 << level;
    }

    public int voxelScale() {
        return 1 << level;
    }

    public int minChunkX() {
        return x << level;
    }

    public int minChunkZ() {
        return z << level;
    }

    public int maxChunkX() {
        return minChunkX() + sizeInChunks() - 1;
    }

    public int maxChunkZ() {
        return minChunkZ() + sizeInChunks() - 1;
    }

    public boolean containsChunk(int chunkX, int chunkZ) {
        return chunkX >= minChunkX() && chunkX <= maxChunkX() && chunkZ >= minChunkZ() && chunkZ <= maxChunkZ();
    }

    // Chebyshev distance in chunks from a chunk to the nearest chunk of this node
    public int distanceInChunks(int chunkX, int chunkZ) {
        int dx = Math.max(0, Math.max(minChunkX() - chunkX, chunkX - maxChunkX()));
        int dz = Math.max(0, Math.max(minChunkZ() - chunkZ, chunkZ - maxChunkZ()));
        return Math.max(dx, dz);
    }

    public Vector3i chunkPosition() {
        return new Vector3i(minChunkX(), 0, minChunkZ());
    }
}
