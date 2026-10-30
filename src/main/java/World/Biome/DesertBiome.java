package World.Biome;

import World.Block;
import org.joml.Vector2d;

/*
 * Hot, dry sand sea. The ground is mostly flat and shaped by long dune crests.
 */
public class DesertBiome extends Biome {

    private static final double DUNE_HEIGHT = 6.0;
    private static final double DUNE_SCALE = 0.025;
    private static final int DUNE_OCTAVES = 2;

    public DesertBiome() {
        super("Desert",
                new Climate(0.7, -0.7, 0.0),
                new TerrainSettings()
                        .baseHeight(64)
                        .hills(3.6, 0.012, 4)
                        .hillVariation(0.65),
                new SurfaceSettings()
                        .top(Block.SAND)
                        .filler(Block.SAND, 5)
                        .noSnow());
    }

    @Override
    protected double shape(Vector2d p, TerrainNoise noise) {
        // Stretching the coordinates turns round ridges into long wind-blown crests
        Vector2d duneP = new Vector2d(p.x * 0.6 + p.y * 0.2, p.y * 1.4);
        return noise.ridged(NoiseLayer.DUNES, duneP, DUNE_SCALE, DUNE_OCTAVES, 0.4, 2.0) * DUNE_HEIGHT;
    }
}
