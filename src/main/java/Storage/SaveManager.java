package Storage;

import Exceptions.NullSaveFileLoadException;
import World.Block;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class SaveManager {

    String worldName;
    Queue<Save> saves;
    private final int SAVE_BACKLOG = 10;
    private FileLoader fileLoader;

    public SaveManager(String worldName)throws IOException
    {
        this.worldName = worldName;
    }

    public void init()throws IOException
    {
        fileLoader = new FileLoader();
        saves = fileLoader.LoadSaves();
    }

    public PriorityQueue<Save> LoadSave() throws IOException, NullSaveFileLoadException {
        FileLoader loader = new FileLoader();
        //load all saves from world directory
        //if file doesnt exist throw IOexception
        //if none exists throw NullSaveFileLoadException
    }

    public void CreateSave(long seed, HashMap<Vector3i, Block> diff) throws IOException{
        FileCreator creator = new FileCreator();
        //if world directory doesnt exist throw IOexception
    }
}
