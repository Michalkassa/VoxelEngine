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
    Save save;
    private final int SAVE_BACKLOG = 10;
    private FileLoader fileLoader;

    public SaveManager(String worldName)
    {
        this.worldName = worldName;
    }

    public void LoadSave(Save save){
        this.save = save;
    }

    public Save getSave(){
        return save;
    }
}
