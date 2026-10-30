package World.Biome;

import java.util.List;

/*
 * Every biome the generator can place. Add new biomes here.
 */
public final class Biomes {

    public static final List<Biome> ALL = List.of(
            new OceanBiome(),
            new PlainsBiome(),
            new HillyPlainsBiome(),
            new DesertBiome(),
            new MountainsBiome()
    );

    private Biomes() {
    }
}
