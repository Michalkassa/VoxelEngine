package World;

import Core.ChunkMesh;
import Core.Frustum;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL46.glUniform1f;
import static org.lwjgl.opengl.GL46.glUniform3f;

public class LodManager {

    private static final int MAX_BUILDS_PER_FRAME = 8;

    private static final class LodMesh {
        final ChunkMesh mesh;
        final int minY;
        final int maxY;

        LodMesh(ChunkMesh mesh, int minY, int maxY) {
            this.mesh = mesh;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private final TerrainGenerator generator;
    private final ChunkManager chunkManager;
    private final int detailDistance;
    private final int renderDistance;
    private final int spread;

    private final Map<LodNode, LodMesh> meshes = new HashMap<>();
    private final Set<LodNode> emptyNodes = new HashSet<>();
    private List<LodNode> selection = new ArrayList<>();
    private final List<LodNode> buildQueue = new ArrayList<>();
    private Vector3i centreChunk;

    private final ArrayList<Chunk> visibleChunks = new ArrayList<>();
    private final ArrayList<LodNode> visibleNodes = new ArrayList<>();

    public LodManager(TerrainGenerator generator, ChunkManager chunkManager, int detailDistance, int renderDistance, int spread) {
        this.generator = generator;
        this.chunkManager = chunkManager;
        this.detailDistance = detailDistance;
        this.renderDistance = renderDistance;
        this.spread = spread;
    }

    public void update(Vector3i playerChunk) {
        if (!playerChunk.equals(centreChunk)) {
            centreChunk = new Vector3i(playerChunk);
            reselect();
        }
        buildQueued(MAX_BUILDS_PER_FRAME);
    }

    public void buildAll() {
        buildQueued(Integer.MAX_VALUE);
    }

    private void reselect() {
        selection = LodSelection.select(centreChunk.x, centreChunk.z, detailDistance, renderDistance, spread);
        Set<LodNode> selected = new HashSet<>(selection);

        meshes.entrySet().removeIf(entry -> {
            if (selected.contains(entry.getKey())) return false;
            entry.getValue().mesh.cleanup();
            return true;
        });
        emptyNodes.retainAll(selected);

        buildQueue.clear();
        for (LodNode node : selection) {
            if (node.level() > 0 && !meshes.containsKey(node) && !emptyNodes.contains(node)) {
                buildQueue.add(node);
            }
        }
        // Build from the player outwards so nearby detail appears first
        buildQueue.sort(Comparator.comparingInt((LodNode node) -> node.distanceInChunks(centreChunk.x, centreChunk.z)).reversed());
    }

    private void buildQueued(int limit) {
        int built = 0;
        while (!buildQueue.isEmpty() && built < limit) {
            LodNode node = buildQueue.remove(buildQueue.size() - 1);
            int[] data = LodMesher.buildVertexData(node, generator);
            built++;
            if (data.length == 0) {
                emptyNodes.add(node);
                continue;
            }
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (int vertex : data) {
                int y = BlockFaces.unpackY(vertex);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            int scale = node.voxelScale();
            meshes.put(node, new LodMesh(new ChunkMesh(data, data.length / ChunkMesh.VERTICES_PER_QUAD), minY * scale, maxY * scale));
        }
    }

    public int getPendingBuilds() {
        return buildQueue.size();
    }

    public int getLodMeshCount() {
        return meshes.size();
    }

    public void render(Frustum frustum, Vector3f cameraPosition, int chunkOffsetUniform, int voxelScaleUniform) {
        visibleChunks.clear();
        visibleNodes.clear();

        for (LodNode node : selection) {
            float minX = node.minChunkX() * Chunk.CHUNK_SIZE - 0.5f;
            float minZ = node.minChunkZ() * Chunk.CHUNK_SIZE - 0.5f;
            float width = node.sizeInChunks() * Chunk.CHUNK_SIZE;

            if (node.level() == 0) {
                Chunk chunk = chunkManager.getChunk(node.chunkPosition());
                if (chunk == null || !chunk.hasVisibleMesh()) continue;
                if (frustum.isBoxVisible(minX, chunk.getMeshMinY() - 0.5f, minZ,
                        minX + width, chunk.getMeshMaxY() - 0.5f, minZ + width)) {
                    visibleChunks.add(chunk);
                }
            } else {
                LodMesh lod = meshes.get(node);
                if (lod == null) continue;
                if (frustum.isBoxVisible(minX, lod.minY - 0.5f, minZ, minX + width, lod.maxY - 0.5f, minZ + width)) {
                    visibleNodes.add(node);
                }
            }
        }

        // Full-detail chunks are always nearer than LOD regions, so draw them first, each list nearest first
        visibleChunks.sort(Comparator.comparingDouble(chunk -> distanceSquared(
                chunk.getChunkPosition().x, chunk.getChunkPosition().z, 1, cameraPosition)));
        visibleNodes.sort(Comparator.comparingDouble(node -> distanceSquared(
                node.minChunkX(), node.minChunkZ(), node.sizeInChunks(), cameraPosition)));

        glUniform1f(voxelScaleUniform, 1f);
        for (Chunk chunk : visibleChunks) {
            Vector3i pos = chunk.getChunkPosition();
            glUniform3f(chunkOffsetUniform, pos.x * Chunk.CHUNK_SIZE, 0, pos.z * Chunk.CHUNK_SIZE);
            chunk.render();
        }

        for (LodNode node : visibleNodes) {
            glUniform1f(voxelScaleUniform, node.voxelScale());
            glUniform3f(chunkOffsetUniform, node.minChunkX() * Chunk.CHUNK_SIZE, 0, node.minChunkZ() * Chunk.CHUNK_SIZE);
            meshes.get(node).mesh.draw();
        }
    }

    private static float distanceSquared(int minChunkX, int minChunkZ, int sizeInChunks, Vector3f point) {
        float half = sizeInChunks * Chunk.CHUNK_SIZE / 2f;
        float dx = minChunkX * Chunk.CHUNK_SIZE + half - point.x;
        float dz = minChunkZ * Chunk.CHUNK_SIZE + half - point.z;
        return dx * dx + dz * dz;
    }

    public void cleanup() {
        for (LodMesh lod : meshes.values()) {
            lod.mesh.cleanup();
        }
        meshes.clear();
    }
}
