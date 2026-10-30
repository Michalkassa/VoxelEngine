package World.Biome;

/*
 * Independent noise fields biomes can sample from. Each layer gets its own seed,
 * so adding a new kind of terrain feature is just a new entry here.
 *
 * Biomes share layers on purpose: two neighbouring biomes that both use HILLS
 * get the same hills, so they blend into each other without seams.
 */
public enum NoiseLayer {
    HILLS,
    HILL_STRENGTH,
    DUNES,
    RIDGES,
    DETAIL
}
