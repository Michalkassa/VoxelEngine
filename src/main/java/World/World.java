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

        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.setBlockAt(new Vector3i(0, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(1, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(2, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(5, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(8, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(12, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(16, 8, 0), (byte)1);
        chunkManager.setBlockAt(new Vector3i(16, 8, 2), (byte)1);
        chunkManager.setBlockAt(new Vector3i(19, 8, 3), (byte)1);
        chunkManager.setBlockAt(new Vector3i(22, 9, 4), (byte)1); // vertical jump
        chunkManager.setBlockAt(new Vector3i(26, 9, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(30, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(33, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(36, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(39, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(42, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(45, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(48, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(51, 8, 4), (byte)1);
        chunkManager.setBlockAt(new Vector3i(54, 8, 4), (byte)1);
    }

    public void render(){
        chunkManager.renderChunks();
    }

    public ChunkManager getChunkManager(){
        return chunkManager;
    }

    public void update(){
        chunkManager.loadChunksInRadius(camera.getChunkPosition(), 3);
        chunkManager.unloadChunksOutOfRadius(camera.getChunkPosition(), 3);
    }

    public void cleanup(){
        chunkManager.cleanup();
    }
}
