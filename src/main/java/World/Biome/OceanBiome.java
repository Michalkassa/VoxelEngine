package World.Biome;

import World.Block;

/*
 * Deep lowland below sea level, so the generator floods it. Broad, gentle swells on a sandy floor.
 */
public class OceanBiome extends Biome {

    public OceanBiome() {
        super("Ocean",
                new Climate(0.0, 0.0, -0.7),
                new TerrainSettings()
                        .baseHeight(36)
                        .hills(6.0, 0.008, 3),
                new SurfaceSettings()
                        .top(Block.SAND)
                        .filler(Block.SAND, 5)
                        .shore(Block.SAND, 5, 2)
                        .noBareRock()
                        .noSnow());
    }
}
