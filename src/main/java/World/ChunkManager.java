package World;

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
    private Queue<Chunk> cleanupQueue = new ConcurrentLinkedQueue<>();
    private final Set<Vector3i> pendingMeshes = new LinkedHashSet<>();
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
            queueMeshCandidates(position);
            buildPendingMeshes(Integer.MAX_VALUE);
        }
    }

    private void queueMeshCandidates(Vector3i position) {
        pendingMeshes.add(position);
        pendingMeshes.add(new Vector3i(position.x + 1, 0, position.z));
        pendingMeshes.add(new Vector3i(position.x - 1, 0, position.z));
        pendingMeshes.add(new Vector3i(position.x, 0, position.z + 1));
        pendingMeshes.add(new Vector3i(position.x, 0, position.z - 1));
    }

    public boolean hasAllNeighbours(Vector3i position) {
        return chunks.containsKey(new Vector3i(position.x + 1, 0, position.z))
                && chunks.containsKey(new Vector3i(position.x - 1, 0, position.z))
                && chunks.containsKey(new Vector3i(position.x, 0, position.z + 1))
                && chunks.containsKey(new Vector3i(position.x, 0, position.z - 1));
    }

    // A chunk is meshed once, when all 4 neighbours exist, so loading never forces a rebuild
    private int buildPendingMeshes(int limit) {
        int built = 0;
        Iterator<Vector3i> iterator = pendingMeshes.iterator();
        while (iterator.hasNext() && built < limit) {
            Vector3i position = iterator.next();
            iterator.remove();
            Chunk chunk = chunks.get(position);
            if (chunk == null || chunk.hasMesh() || !hasAllNeighbours(position)) continue;
            chunk.buildMesh();
            built++;
        }
        return built;
    }

    public Chunk getChunk(Vector3f world_position) {
        int chunkX = Math.floorDiv((int) Math.floor(world_position.x + 0.5f), Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv((int) Math.floor(world_position.z + 0.5f), Chunk.CHUNK_SIZE);
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
        if (chunk.hasMesh()) {
            chunk.buildMesh();
        }

        if (localCoordinates.x == 0) rebuildMeshedChunk(chunkCoordinates.x - 1, chunkCoordinates.z);
        if (localCoordinates.x == Chunk.CHUNK_SIZE - 1) rebuildMeshedChunk(chunkCoordinates.x + 1, chunkCoordinates.z);
        if (localCoordinates.z == 0) rebuildMeshedChunk(chunkCoordinates.x, chunkCoordinates.z - 1);
        if (localCoordinates.z == Chunk.CHUNK_SIZE - 1) rebuildMeshedChunk(chunkCoordinates.x, chunkCoordinates.z + 1);
    }

    private void rebuildMeshedChunk(int chunkX, int chunkZ) {
        Chunk chunk = getChunk(new Vector3i(chunkX, 0, chunkZ));
        if (chunk != null && chunk.hasMesh()) {
            chunk.buildMesh();
        }
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

    public void loadChunkAsync(Vector3i position) {
        if (!chunks.containsKey(position)) {
            byte[][][] generatedBlocks = terrainGenerator.generateChunkTerrain(position);
            Chunk chunk = new Chunk(position, this, generatedBlocks);
            chunks.put(position, chunk);
            chunksToBuild.add(position);
        }
    }

    public void buildQueuedMeshes() {
        Chunk toClean;
        while((toClean = cleanupQueue.poll()) != null) {
            toClean.cleanup();
        }

        int maxMeshesPerFrame = 2;

        Vector3i loaded;
        while ((loaded = chunksToBuild.poll()) != null) {
            queueMeshCandidates(loaded);
        }
        buildPendingMeshes(maxMeshesPerFrame);
    }

    public void unloadChunk(Vector3i position) {
        Chunk chunk = chunks.remove(position);
        if (chunk != null) {
            cleanupQueue.add(chunk);
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