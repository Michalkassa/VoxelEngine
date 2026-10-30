package World.Biome;

import World.Block;
import org.joml.Vector2d;

/*
 * Grassy uplands of broad, rounded hills. Sits between plains and mountains in
 * elevation, so the land steps up gradually instead of jumping straight into peaks.
 */
public class HillyPlainsBiome extends Biome {

    // Hills reach this fraction of the mountains' height
    private static final double SIZE_OF_MOUNTAINS = 0.33;
    private static final double BASE_LIFT = MountainsBiome.BASE_LIFT * SIZE_OF_MOUNTAINS;
    private static final double HILL_HEIGHT = MountainsBiome.PEAK_HEIGHT * SIZE_OF_MOUNTAINS;
    // Same ridge field and width as the mountains, so hills line up with peaks where the two meet
    private static final double HILL_SCALE = MountainsBiome.RIDGE_SCALE;
    // Fewer octaves than the mountains keeps the hills smooth
    private static final int HILL_OCTAVES = 3;

    public HillyPlainsBiome() {
        super("Hilly Plains",
                new Climate(0.0, 0.15, 0.5),
                new TerrainSettings()
                        .baseHeight(64)
                        .hills(12.0, 0.012, 4)
                        .hillVariation(0.3),
                new SurfaceSettings()
                        .top(Block.GRASS)
                        .filler(Block.DIRT, 4));
    }

    @Override
    protected double shape(Vector2d p, TerrainNoise noise) {
        return BASE_LIFT + noise.ridged(NoiseLayer.RIDGES, p, HILL_SCALE, HILL_OCTAVES, 0.5, 2.1) * HILL_HEIGHT;
    }
}
