package World;

import org.joml.Vector3i;

public class RandomNoise {
    private final long seed;
    private final int MAX_HEIGHT = 35;
    private final int MIN_HEIGHT = 32;

    public RandomNoise(long seed) {
        this.seed = seed;
    }

    public double noise(double x, double z) {
        // Add scale for smoother terrain
        x *= 0.1;
        z *= 0.1;

        int X = (int)Math.floor(x);
        int Z = (int)Math.floor(z);

        x -= Math.floor(x);
        z -= Math.floor(z);

        double u = x * x * (3 - 2 * x);
        double v = z * z * (3 - 2 * z);

        long h1 = hash(X, Z);
        long h2 = hash(X + 1, Z);
        long h3 = hash(X, Z + 1);
        long h4 = hash(X + 1, Z + 1);

        double g1 = (h1 & 1) == 0 ? x : -x;
        double g2 = (h2 & 1) == 0 ? x - 1 : -(x - 1);
        double g3 = (h3 & 1) == 0 ? x : -x;
        double g4 = (h4 & 1) == 0 ? x - 1 : -(x - 1);

        double l1 = g1 + u * (g2 - g1);
        double l2 = g3 + u * (g4 - g3);

        return l1 + v * (l2 - l1);
    }

    private long hash(int x, int z) {
        long h = seed;
        h = h * 374761393L + x;
        h = h * 668265263L + z;
        h = (h ^ (h >> 13)) * 1274126177L;
        return h ^ (h >> 16);
    }

    public int getHeight(Vector3i position) {
        double noiseValue = noise(position.x, position.z);
        // Normalize from [-1, 1] to [0, 1]
        noiseValue = (noiseValue + 1.0) * 0.5;
        // Map to height range
        return MIN_HEIGHT + (int)(MAX_HEIGHT * noiseValue);
    }
}