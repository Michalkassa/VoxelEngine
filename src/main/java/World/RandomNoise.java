package World;

import org.joml.Vector3i;

import java.util.Random;

public class RandomNoise {
    private final long seed;
    private final int MAX_HEIGHT = 4;
    private  final int MIN_HEIGHT = 1;

    public RandomNoise(long seed) {
        this.seed = seed;

    }

    public double noise(int x, int z) {
        long hash = ((long)x * 374761393L + (long)z * 668265263L) ^ seed;
        Random r = new Random(hash);
        return r.nextDouble();
    }

    public int getHeight(Vector3i position){
        return MIN_HEIGHT + (int)(MAX_HEIGHT * noise(position.x,position.z));
    }
}
