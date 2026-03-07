package World;

import Core.Camera;
import Exceptions.NullSaveFileLoadException;
import Storage.FileCreator;
import Storage.FileLoader;
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

    public World(String name, Camera camera, long seed) {
        this.name = name;
        this.seed = seed;
        this.camera = camera;

        if(FileCreator.worldExists(name)){
            try{
                Save save = FileLoader.LoadSave(name);
                saveManager.LoadSave(save);
            }catch(IOException | NullSaveFileLoadException e){
                System.err.println("Cannot Load save from world: " + name);
                System.err.println(e.getMessage());
            }
        }else{
            try{
                FileCreator.createWorldDirectory(name);
            }catch (IOException e){
                System.err.println("Cannot create world: " + name);
                System.err.println(e.getMessage());
            }
        }

        this.chunkManager = new ChunkManager(seed);
        this.saveManager = new SaveManager(name);

        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 10);
    }

    public void render(){
        chunkManager.renderChunks();
    }

    public ChunkManager getChunkManager(){
        return chunkManager;
    }

    public void update(){
        chunkManager.update();
        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 10);
        chunkManager.unloadChunksOutOfRadius(camera.getChunkPosition(), 10);
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
        chunkManager.cleanup();
    }

}
