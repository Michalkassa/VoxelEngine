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
    }

    public void render(){
        chunkManager.renderChunks();
    }

    public void update(){
        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.unloadChunksOutOfRadius(camera.getChunkPosition(), 3);
    }

    public void cleanup(){
        chunkManager.cleanup();
    }
}
