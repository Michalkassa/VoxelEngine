package World;

import Core.Camera;
import Storage.FileCreator;
import Storage.Save;
import Storage.SaveManager;
import org.joml.Vector3i;

import java.io.IOException;

public class World {
    private String name;
    private long seed;
    private ChunkManager chunkManager;
    private Camera camera;
    private SaveManager saveManager;

    public World(String name, Camera camera, long seed)throws IOException {
        this.name = name;
        this.seed = seed;
        this.camera = camera;
        FileCreator.createDirectory(name);
        this.chunkManager = new ChunkManager(seed);
        this.saveManager = new SaveManager(name);
    }

    public void update(){
        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.unloadChunksOutOfRadius(camera.getChunkPosition(), 3);
    }

    public void cleanup(){
        chunkManager.cleanup();
    }
}
