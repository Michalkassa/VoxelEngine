package World.Biome;

import World.PerlinNoise;
import org.joml.Vector2d;

import java.util.EnumMap;
import java.util.Map;

/*
 * Seeded noise handed to biomes when they shape their terrain.
 * Read-only after construction, so it is safe to share between worker threads.
 */
public class TerrainNoise {

    // Raw octave noise mostly sits within +-0.25, this stretches it to roughly [-1, 1]
    private static final double NOISE_NORMALIZE = 4.0;

    // Keeps layer seeds clear of the seeds the generator uses for climate and warp
    private static final long LAYER_SEED_OFFSET = 100;

    private final Map<NoiseLayer, PerlinNoise> layers = new EnumMap<>(NoiseLayer.class);

    public TerrainNoise(long seed) {
        for (NoiseLayer layer : NoiseLayer.values()) {
            layers.put(layer, new PerlinNoise(seed + LAYER_SEED_OFFSET + layer.ordinal()));
        }
    }

    // Smooth fractal noise in [-1, 1]
    public double fbm(NoiseLayer layer, Vector2d p, double scale, int octaves) {
        return fbm(layers.get(layer), p, scale, octaves);
    }

    // Sharp crests in [0, 1], for ridges and dunes
    public double ridged(NoiseLayer layer, Vector2d p, double scale, int octaves, double persistence, double lacunarity) {
        return layers.get(layer).getRidgedNoise(p, scale, octaves, persistence, lacunarity);
    }

    public static double fbm(PerlinNoise noise, Vector2d p, double scale, int octaves) {
        double value = noise.getOctaveNoise(p, scale, octaves, 0.5, 2.0) * NOISE_NORMALIZE;
        return Math.max(-1.0, Math.min(1.0, value));
    }

    public static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }
}
