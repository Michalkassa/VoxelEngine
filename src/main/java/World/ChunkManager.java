package World;

import Core.Frustum;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.lang.Math;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkManager {

    private Map<Vector3i, Chunk> chunks;
    private TerrainGenerator terrainGenerator;
    private Queue<Vector3i> chunksToBuild = new ConcurrentLinkedQueue<>();
    private Map<Vector3i, Chunk> chunksToCleanup = new ConcurrentHashMap<>();
    private long seed;

    public ChunkManager(long seed) {
        this.chunks = new ConcurrentHashMap<>();
        this.terrainGenerator = new TerrainGenerator(seed);
    }

    public void loadChunk(Vector3i position) {
        if (!chunks.containsKey(position)) {
            byte[][][] generatedBlocks = terrainGenerator.generateChunkTerrain(position);
            Chunk chunk = new Chunk(position, this, generatedBlocks);
            chunks.put(position, chunk);
            chunk.buildMesh();
            rebuildAdjacentChunks(position);
        }
    }

    public Chunk getChunk(Vector3f world_position) {
        int chunkX = Math.floorDiv((int) world_position.x, Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv((int) world_position.z, Chunk.CHUNK_SIZE);
        return getChunk(new Vector3i(chunkX, 0, chunkZ));
    }

    public Chunk getChunk(Vector3i position) {
        return chunks.get(position);
    }

    public boolean isBlockAt(Vector3i world_position) {
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), 0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y, Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if (chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT) {
            return false;
        }

        try {
            return chunk.getBlock(localCoordinates.x, localCoordinates.y, localCoordinates.z) != 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public byte getBlockAt(Vector3i world_position) {
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), 0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y, Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if (chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT) {
            return 0;
        }

        try {
            return chunk.getBlock(localCoordinates.x, localCoordinates.y, localCoordinates.z);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public void setBlockAt(Vector3i world_position, byte block_type) {
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), 0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y, Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if (chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT) {
            throw new RuntimeException("Trying to set block that doesnt Exist");
        }
        chunk.setBlock(localCoordinates, block_type);
        rebuildAdjacentChunks(chunkCoordinates);
    }

    public void loadChunksInRadius(Vector3i centre, int radius) {
        for (int x = centre.x - radius; x <= centre.x + radius; x++) {
            for (int z = centre.z - radius; z <= centre.z + radius; z++) {
                Vector3i pos = new Vector3i(x, 0, z);
                if (!chunks.containsKey(pos)) {
                    loadChunk(pos);
                }
            }
        }
    }

    public void unloadChunksOutOfRadius(Vector3i centre, int radius) {
        List<Vector3i> chunksToUnload = new ArrayList<>();

        for (Vector3i chunkPos : chunks.keySet()) {
            int deltaX = Math.abs(chunkPos.x - centre.x);
            int deltaZ = Math.abs(chunkPos.z - centre.z);

            if (deltaX > radius || deltaZ > radius) {
                chunksToUnload.add(chunkPos);
            }
        }

        for (Vector3i chunkPos : chunksToUnload) {
            unloadChunk(chunkPos);
        }
    }

    private void rebuildAdjacentChunks(Vector3i position) {
        Vector3i[] neighbors = {
                new Vector3i(position.x + 1, 0, position.z),
                new Vector3i(position.x - 1, 0, position.z),
                new Vector3i(position.x, 0, position.z + 1),
                new Vector3i(position.x, 0, position.z - 1)
        };

        for (Vector3i neighbor : neighbors) {
            Chunk chunk = getChunk(neighbor);
            if (chunk != null) {
                chunk.buildMesh();
            }
        }
    }

    public void renderChunks(Frustum frustum) {
        for (Chunk chunk : chunks.values()) {
            Vector3i pos = chunk.getChunkPosition();
            float minX = pos.x * Chunk.CHUNK_SIZE;
            float minY = pos.y * Chunk.CHUNK_HEIGHT;
            float minZ = pos.z * Chunk.CHUNK_SIZE;
            float maxX = minX + Chunk.CHUNK_SIZE;
            float maxY = minY + Chunk.CHUNK_HEIGHT;
            float maxZ = minZ + Chunk.CHUNK_SIZE;

            if (frustum.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                chunk.render();
            }
        }
    }

    public void loadChunkAsync(Vector3i position) {
        if (!chunks.containsKey(position)) {
            byte[][][] generatedBlocks = terrainGenerator.generateChunkTerrain(position);
            Chunk chunk = new Chunk(position, this, generatedBlocks);
            chunks.put(position, chunk);
            chunksToBuild.add(position);
        }
    }

    public void buildQueuedMeshes() {
        int meshesBuilt = 0;
        int maxMeshesPerFrame = 2;

        while (!chunksToBuild.isEmpty() && meshesBuilt < maxMeshesPerFrame) {
            Vector3i chunkPos = chunksToBuild.poll();
            if (chunkPos != null) {
                Chunk chunk = chunks.get(chunkPos);
                if (chunk != null) {
                    chunk.buildMesh();
                    rebuildAdjacentChunks(chunkPos);
                    meshesBuilt++;
                }
            }
        }
    }

    public void unloadChunk(Vector3i position) {
        Chunk chunk = chunks.remove(position);
        if (chunk != null) {
            chunksToCleanup.put(position, chunk);
        }
    }

    public void cleanupQueuedChunks() {
        int chunksCleanedUp = 0;
        int maxCleanupsPerFrame = 4;

        for (Vector3i pos : chunksToCleanup.keySet()) {
            if (chunksCleanedUp >= maxCleanupsPerFrame) break;
            Chunk chunk = chunksToCleanup.remove(pos);
            if (chunk != null) {
                chunk.cleanup();
                chunksCleanedUp++;
            }
        }
    }

    public boolean isChunkLoaded(Vector3i position) {
        return chunks.containsKey(position);
    }

    public Set<Vector3i> getLoadedChunks() {
        return new HashSet<>(chunks.keySet());
    }

    public void cleanup() {
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
    }
}