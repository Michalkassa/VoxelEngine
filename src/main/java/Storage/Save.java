package Storage;

import World.Block;
import org.joml.Vector3i;

import java.util.Date;
import java.util.HashMap;


public class Save {

    private final String seed;
    private Date date;
    HashMap<Vector3i, Block> diff; //stores the

    public Save(String seed){
        this.seed = seed;
    }

}
