package Storage;

import Exceptions.NullSaveFileLoadException;

import java.io.IOException;
import java.util.PriorityQueue;

public class FileLoader {

    public PriorityQueue<Save> LoadSaves() throws IOException, NullSaveFileLoadException {
        FileLoader loader = new FileLoader();
        //load all saves from world directory
        //if file doesnt exist throw IOexception
        //if none exists throw NullSaveFileLoadException
    }
}
