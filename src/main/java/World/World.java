package World;

import java.io.IOException;

import org.joml.Vector3f;
import org.joml.Vector3i;

import Core.Frustum;
import Entity.Player;
import Storage.FileCreator;
import Storage.SaveManager;

public class World {
    private final String name;
    private final long seed;
    private final ChunkManager chunkManager;
    private final SaveManager saveManager;
    private final ChunkLoader chunkLoader;
    private final Player player;
    private final LodManager lodManager;
    // Afternoon sun, high enough to light the tops brightest and off-axis so each side gets a different shade
    private final Sun sun = new Sun(50f, 30f);
    // Full detail out to 32 chunks, then 2x blocks from 32, 4x from 64, 8x from 128 and 16x from 256 chunks.
    // A larger spread pushes the coarser levels further out.
    private final int DETAIL_DISTANCE = 32;
    private final int LOD_SPREAD = 1;
    private final int RENDER_DISTANCE = 128;

    public World(String name, Player player, long seed) {
        this.name = name;
        this.seed = seed;
        this.player = player;
        this.chunkManager = new ChunkManager(seed);
        this.saveManager = new SaveManager(name);

        if (!FileCreator.worldExists(name)) {
            try {
                FileCreator.createWorldDirectory(name);
                System.out.println("Created new world: " + name);
            } catch (IOException e){
                System.err.println("Cannot create world: " + name);
                System.err.println(e.getMessage());
            }
        }

        // Full chunks are only kept near the player, plus one ring so edge chunks can be meshed
        Vector3i spawnChunk = new Vector3i(0, 0, 0);
        chunkManager.loadChunksInRadius(spawnChunk, DETAIL_DISTANCE + 1);

        lodManager = new LodManager(new TerrainGenerator(seed), chunkManager, DETAIL_DISTANCE, RENDER_DISTANCE, LOD_SPREAD);
        lodManager.update(spawnChunk);
        lodManager.buildAll();

        chunkLoader = new ChunkLoader(chunkManager, player, DETAIL_DISTANCE + 1);
        chunkLoader.start();
    }

    public void render(Frustum frustum, Vector3f cameraPosition, int chunkOffsetUniform, int voxelScaleUniform) {
        lodManager.render(frustum, cameraPosition, chunkOffsetUniform, voxelScaleUniform);
    }

    public int getRenderDistance() {
        return RENDER_DISTANCE;
    }

    public Sun getSun() {
        return sun;
    }

    private Vector3i playerChunk() {
        Vector3f position = player.getPosition();
        return new Vector3i(
                Math.floorDiv((int) Math.floor(position.x + 0.5f), Chunk.CHUNK_SIZE),
                0,
                Math.floorDiv((int) Math.floor(position.z + 0.5f), Chunk.CHUNK_SIZE));
    }

    public ChunkManager getChunkManager(){
        return chunkManager;
    }

    public void update(){
        chunkManager.buildQueuedMeshes();
        lodManager.update(playerChunk());
    }

    public int getSurfaceHeight(Vector3i worldPosition){
        for (int y = Chunk.CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (chunkManager.isBlockAt(new Vector3i(worldPosition.x, y, worldPosition.z))) {
                return y;
            }
        }
        return 0;
    }

    public void cleanup(){
        chunkLoader.stopLoading();
        chunkManager.cleanup();
        lodManager.cleanup();
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

}