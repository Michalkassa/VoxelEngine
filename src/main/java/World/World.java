package World;

import Core.Camera;
import Core.Frustum;
import Entity.Player;
import Storage.FileCreator;
import Storage.SaveManager;
import org.joml.Vector3i;

import java.io.IOException;

public class World {
    private String name;
    private long seed;
    private ChunkManager chunkManager;
    private Camera camera;
    private SaveManager saveManager;
    private ChunkLoader chunkLoader;
    private Player player;
    private final int INITIAL_RENDER_DISTANCE = 20;
    private final int RENDER_DISTANCE = 40;

    public World(String name, Camera camera, Player player, long seed) {
        this.name = name;
        this.seed = seed;
        this.camera = camera;
        this.player = player;
        this.chunkManager = new ChunkManager(seed);
        this.saveManager = new SaveManager(name);

        // Create world directory if needed
        if (!FileCreator.worldExists(name)) {
            try {
                FileCreator.createWorldDirectory(name);
                System.out.println("Created new world: " + name);
            } catch (IOException e){
                System.err.println("Cannot create world: " + name);
                System.err.println(e.getMessage());
            }
        }

        // Load initial chunks around spawn
        Vector3i spawnChunk = new Vector3i(0, 0, 0);
        chunkManager.loadChunksInRadius(spawnChunk, INITIAL_RENDER_DISTANCE);

        // Start chunk loader thread
        chunkLoader = new ChunkLoader(chunkManager, player, RENDER_DISTANCE);
        chunkLoader.start();
    }

    public void render(Frustum frustum) {
        chunkManager.renderChunks(frustum);
    }

    public ChunkManager getChunkManager(){
        return chunkManager;
    }

    public void update(){
        // ChunkLoader thread handles chunk loading/unloading automatically
        chunkManager.buildQueuedMeshes();
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
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

}