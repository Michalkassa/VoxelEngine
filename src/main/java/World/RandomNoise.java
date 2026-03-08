package World;

import org.joml.Vector3i;

import java.util.HashMap;

public class RandomNoise {
    private final long seed;
    private final int MAX_HEIGHT = 100;
    private final int MIN_HEIGHT = 1;

    private final HashMap<Long, Integer> heightCache = new HashMap<>();

    public RandomNoise(long seed) {
        this.seed = seed;
    }

    public double noise(double x, double z) {
        double amplitude = 1.0;
        double frequency = 0.005;
        double result = 0;
        double maxValue = 0;

        // 4 octaves of value noise
        for (int i = 0; i < 4; i++) {
            result += valueNoise(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }

        return result / maxValue;
    }

    private double valueNoise(double x, double z) {
        int X = (int) Math.floor(x);
        int Z = (int) Math.floor(z);

        double fx = x - Math.floor(x);
        double fz = z - Math.floor(z);

        double ux = fx * fx * fx * (fx * (fx * 6 - 15) + 10);
        double uz = fz * fz * fz * (fz * (fz * 6 - 15) + 10);

        double v00 = hashToFloat(X,     Z);
        double v10 = hashToFloat(X + 1, Z);
        double v01 = hashToFloat(X,     Z + 1);
        double v11 = hashToFloat(X + 1, Z + 1);

        double x1 = v00 + ux * (v10 - v00);
        double x2 = v01 + ux * (v11 - v01);

        return x1 + uz * (x2 - x1);
    }

    private double hashToFloat(int x, int z) {
        long h = seed;
        h ^= x * 1619L;
        h ^= z * 31337L;
        h = (h * h * 60493L + h * 19990303L + 1376312589L) & 0x7fffffff;
        return h / (double) 0x7fffffff;
    }

    public int getHeight(int x, int z) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);

        if (heightCache.containsKey(key)) {
            return heightCache.get(key);
        }

        double n = noise(x, z);
        n = Math.pow(n, 2);
        int height = MIN_HEIGHT + (int)(n * (MAX_HEIGHT - MIN_HEIGHT));

        heightCache.put(key, height);
        return height;
    }
}