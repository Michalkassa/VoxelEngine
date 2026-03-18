package World;

import Core.Camera;
import Core.Frustum;
import Entity.Player;
import Storage.FileCreator;
import Storage.SaveManager;
import org.joml.Vector3i;

import java.io.IOException;

public class World {
    private final String name;
    private final long seed;
    private final ChunkManager chunkManager;
    private final SaveManager saveManager;
    private final ChunkLoader chunkLoader;
    private final Player player;
    private final int INITIAL_RENDER_DISTANCE = 32;
    private final int RENDER_DISTANCE = 32;

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

        Vector3i spawnChunk = new Vector3i(0, 0, 0);
        chunkManager.loadChunksInRadius(spawnChunk, INITIAL_RENDER_DISTANCE);

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