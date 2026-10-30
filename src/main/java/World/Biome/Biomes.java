package World.Biome;

import java.util.List;

/*
 * Every biome the generator can place. Add new biomes here.
 */
public final class Biomes {

    public static final List<Biome> ALL = List.of(
            new PlainsBiome(),
            new DesertBiome(),
            new MountainsBiome()
    );

    private Biomes() {
    }
}
