package World.Biome;

import World.Block;
import org.joml.Vector2d;

/*
 * Rare highland ranges of ridged peaks. Bare rock high up, snow on the tallest summits.
 */
public class MountainsBiome extends Biome {

    // Lifts the whole range above the surrounding land before peaks are added
    static final double BASE_LIFT = 45.0;
    static final double PEAK_HEIGHT = 140.0;
    // Smaller scale = wider ridges
    static final double RIDGE_SCALE = 0.006 / 1.3;
    private static final int RIDGE_OCTAVES = 5;
    // Above 1 sharpens the ridges so only the tallest points poke above the snow line
    private static final double PEAK_SHARPNESS = 1.3;

    public MountainsBiome() {
        super("Mountains",
                new Climate(0.0, 0.0, 1.1),
                new TerrainSettings()
                        .baseHeight(64)
                        .hills(12.0, 0.012, 4),
                new SurfaceSettings()
                        .top(Block.GRASS)
                        .filler(Block.DIRT, 4)
                        .bareSoilOnSlopes(2, 5)
                        .stoneOnSlopes(3, 6)
                        .bareRockAbove(105, 135)
                        .patchiness(0.09, 0.3)
                        .snowAbove(165, 10.0, 7));
    }

    @Override
    protected double shape(Vector2d p, TerrainNoise noise) {
        double ridges = noise.ridged(NoiseLayer.RIDGES, p, RIDGE_SCALE, RIDGE_OCTAVES, 0.5, 2.1);
        return BASE_LIFT + Math.pow(ridges, PEAK_SHARPNESS) * PEAK_HEIGHT;
    }
}
