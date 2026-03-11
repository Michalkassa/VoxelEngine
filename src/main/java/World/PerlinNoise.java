package World;

import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Random;

/*
 * -----------------------------------------------------------------------------
 * Portions of this code are adapted from:
 * Repository: https://github.com/PavlosMak/PerlinNoise
 * Author: PavlosMak
 *
 * Modifications made by: Michal Kassa
 * Date: 2026-03-11
 *
 * Description:
 * - This file implements Perlin Noise using JOML vector math with double precision.
 * - Supports octave (fBm) noise for Minecraft-like terrain generation.
 *
 * Note: Please refer to the original repository for full license terms.
 * -----------------------------------------------------------------------------
 */

public class PerlinNoise {

    public static final double DEFAULT_SCALE       = 0.015;
    public static final int    DEFAULT_OCTAVES     = 5;
    public static final double DEFAULT_PERSISTENCE = 0.125;
    public static final double DEFAULT_LACUNARITY  = 5;

    // Gradient and permutation table sizes (must be powers of 2)
    private static final int GRADIENT_SIZE    = 256;
    private static final int PERMUTATION_SIZE = 512;

    // This array stores our random gradients
    private final Vector3d[] gradients = new Vector3d[GRADIENT_SIZE];

    // Permutation table used by Ken Perlin
    private final int[] permutations = new int[PERMUTATION_SIZE];

    /**
     * Creates a new PerlinNoise object with a specific seed.
     *
     * @param seed Seed for random gradient generation.
     */
    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        initialiseGradients(random);
        initialisePermutations();
    }

    private void initialiseGradients(Random random) {
        for (int i = 0; i < GRADIENT_SIZE; i++) {
            gradients[i] = getRandomNormalizedVector3d(random);
        }
    }

    private void initialisePermutations() {
        int[] p = {151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,
                103,30,69,142,8,99,37,240,21,10,23,190,6,148,247,120,234,75,0,
                26,197,62,94,252,219,203,117,35,11,32,57,177,33,88,237,149,56,
                87,174,20,125,136,171,168,68,175,74,165,71,134,139,48,27,166,
                77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,
                46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,
                187,208,89,18,169,200,196,135,130,116,188,159,86,164,100,109,
                198,173,186,3,64,52,217,226,250,124,123,5,202,38,147,118,126,
                255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,223,183,
                170,213,119,248,152,2,44,154,163,70,221,153,101,155,167,43,
                172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,
                104,218,246,97,228,251,34,242,193,238,210,144,12,191,179,162,
                241,81,51,145,235,249,14,239,107,49,192,214,31,181,199,106,
                157,184,84,204,176,115,121,50,45,127,4,150,254,138,236,205,
                93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180};

        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            permutations[i] = p[i % GRADIENT_SIZE];
        }
    }

    private Vector3d getRandomNormalizedVector3d(Random random) {
        double theta = Math.acos(2.0 * random.nextDouble() - 1.0);
        double phi = 2.0 * Math.PI * random.nextDouble();

        double x = Math.sin(theta) * Math.cos(phi);
        double y = Math.sin(theta) * Math.sin(phi);
        double z = Math.cos(theta);

        return new Vector3d(x, y, z).normalize();
    }

    private double lerp(double start, double end, double weight) {
        double f = smootherStep(weight);
        return start * (1 - f) + end * f;
    }

    private double smootherStep(double x) {
        if (x <= 0) return 0.0;
        if (x >= 1) return 1.0;
        return x * x * x * (x * (x * 6 - 15) + 10);
    }

    private int hash(int x, int y, int z) {
        return permutations[(permutations[(permutations[x & (GRADIENT_SIZE - 1)] + y) & (GRADIENT_SIZE - 1)] + z) & (GRADIENT_SIZE - 1)];
    }

    /**
     * Raw single-octave 2D Perlin noise. Z is fixed at 0.
     * Returns approximately [-1, 1] — used internally by octave noise.
     */
    private double rawNoise(double nx, double nz) {
        int xi0 = (int) Math.floor(nx) & 255;
        int zi0 = (int) Math.floor(nz) & 255;
        int xi1 = (xi0 + 1) & 255;
        int zi1 = (zi0 + 1) & 255;

        double xf = nx - Math.floor(nx);
        double zf = nz - Math.floor(nz);

        Vector3d[] displacement = new Vector3d[]{
                new Vector3d(xf,       0, zf      ),  // x0 z0
                new Vector3d(xf,       0, zf - 1.0),  // x0 z1
                new Vector3d(xf - 1.0, 0, zf      ),  // x1 z0
                new Vector3d(xf - 1.0, 0, zf - 1.0),  // x1 z1
        };

        double d00 = gradients[hash(xi0, 0, zi0)].dot(displacement[0]);
        double d01 = gradients[hash(xi0, 0, zi1)].dot(displacement[1]);
        double d10 = gradients[hash(xi1, 0, zi0)].dot(displacement[2]);
        double d11 = gradients[hash(xi1, 0, zi1)].dot(displacement[3]);

        double x0 = lerp(d00, d10, xf);
        double x1 = lerp(d01, d11, xf);

        return lerp(x0, x1, zf);
    }

    /**
     * Single-octave Perlin noise at a 2D point.
     * Make sure to scale your coordinates — integer inputs always return 0.
     *
     * @param point Pre-scaled 2D coordinate
     * @return Noise value clamped to [-1, 1]
     */
    public float getPerlinNoiseAtPoint(Vector2d point) {
        return (float) Math.max(-1.0, Math.min(1.0, rawNoise(point.x, point.y)));
    }

    /**
     * Octave (fBm) Perlin noise at a 2D point — Minecraft-style terrain noise.
     *
     * Layers multiple noise samples at increasing frequencies and decreasing
     * amplitudes to produce natural-looking terrain with both broad shapes and
     * fine surface detail.
     *
     * Recommended Minecraft-like settings:
     *   scale       = 0.004  — broad rolling hills (~250 block wavelength)
     *   octaves     = 6      — 6 layers of detail
     *   persistence = 0.5    — each octave is half as strong
     *   lacunarity  = 2.0    — each octave is twice as detailed
     *
     * @param point       World-space 2D coordinate (raw integer grid coords are fine)
     * @param scale       Base frequency — smaller = broader features (try 0.003–0.007)
     * @param octaves     Number of noise layers (4 = smooth, 8 = very detailed)
     * @param persistence Amplitude falloff per octave (0.5 is standard)
     * @param lacunarity  Frequency growth per octave (2.0 is standard)
     * @return Noise value normalized to [-1, 1]
     */
    public float getOctaveNoise(Vector2d point, double scale, int octaves, double persistence, double lacunarity) {
        double value     = 0.0;
        double amplitude = 1.0;
        double frequency = scale;
        double maxValue  = 0.0;

        for (int i = 0; i < octaves; i++) {
            value    += rawNoise(point.x * frequency, point.y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return (float) Math.max(-1.0, Math.min(1.0, value / maxValue));
    }

    /**
     * Octave noise with Minecraft-like defaults:
     * scale=0.004, octaves=6, persistence=0.5, lacunarity=2.0
     *
     * Pass raw world-space coordinates directly — no pre-scaling needed.
     *
     * @param point World-space 2D coordinate
     * @return Noise value normalized to [-1, 1]
     */
    public float getOctaveNoise(Vector2d point) {
        return getOctaveNoise(point, DEFAULT_SCALE, DEFAULT_OCTAVES, DEFAULT_PERSISTENCE, DEFAULT_LACUNARITY);
    }
}