package World.Biome;

import World.Block;

/*
 * Grassy lowland with small rolling hills that flatten out in places.
 */
public class PlainsBiome extends Biome {

    public PlainsBiome() {
        super("Plains",
                new Climate(0.0, 0.3, 0.1),
                new TerrainSettings()
                        .baseHeight(64)
                        .hills(12.0, 0.012, 4)
                        .hillVariation(0.65),
                new SurfaceSettings()
                        .top(Block.GRASS)
                        .filler(Block.DIRT, 4));
    }
}
