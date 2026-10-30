package World.Biome;

import World.Block;
import World.TerrainGenerator.Column;
import org.joml.Vector2d;

/*
 * Base class for every biome. A biome decides three things:
 *  - where it appears : its preferred Climate
 *  - how tall it is   : TerrainSettings, plus anything extra from shape()
 *  - what it's made of: SurfaceSettings, or a custom surface()
 *
 * To add a biome, subclass this, fill in the settings in the constructor and
 * register it in Biomes.ALL. Heights near biome borders are blended by the
 * generator, so a biome never needs to care about its neighbours.
 */
public abstract class Biome {
    private final String name;
    private final Climate climate;
    protected final TerrainSettings terrain;
    protected final SurfaceSettings surface;

    protected Biome(String name, Climate climate, TerrainSettings terrain, SurfaceSettings surface) {
        this.name = name;
        this.climate = climate;
        this.terrain = terrain;
        this.surface = surface;
    }

    public String getName() {
        return name;
    }

    public Climate getClimate() {
        return climate;
    }

    // Ground height of this biome on its own, before blending with neighbours
    public double height(Vector2d p, TerrainNoise noise) {
        double hillStrength = 1.0 - terrain.hillVariation()
                + terrain.hillVariation() * TerrainNoise.smoothstep(-0.25, 0.25, noise.fbm(NoiseLayer.HILL_STRENGTH, p, 0.003, 2));
        double hills = noise.fbm(NoiseLayer.HILLS, p, terrain.hillScale(), terrain.hillOctaves())
                * terrain.hillAmplitude() * hillStrength;
        return terrain.baseHeight() + hills + shape(p, noise);
    }

    // Extra terrain unique to a biome, such as dunes or mountain ridges
    protected double shape(Vector2d p, TerrainNoise noise) {
        return 0.0;
    }

    // Picks the blocks of the column at world x, z. detail is a small noise value in [-1, 1] for breaking up hard lines.
    public Column surface(int x, int z, int height, int slope, double detail, TerrainNoise noise) {
        if (surface.snowLine() != SurfaceSettings.NEVER) {
            int snowLine = (int) (surface.snowLine() + detail * surface.snowLineJitter());
            if (height >= snowLine && slope < surface.cliffSlope()) {
                return new Column(height, Block.SNOW, Block.SNOW, Math.min(4, 1 + (height - snowLine) / 10));
            }
        }

        // One roll per column decides both rules, so where they overlap stone patches sit inside bare soil patches
        double roll = transitionRoll(x, z, noise);

        double stoneChance = Math.max(
                ramp(slope, surface.stoneSlopeStart(), surface.stoneSlopeFull()),
                ramp(height, surface.rockHeightStart(), surface.rockHeightFull()));
        if (roll < stoneChance) {
            return new Column(height, Block.STONE, Block.STONE, 0);
        }

        double thinning = ramp(slope, 0, surface.stoneSlopeFull());
        int depth = (int) Math.round(surface.fillerDepth() - (surface.fillerDepth() - surface.minFillerDepth()) * thinning);

        if (roll < ramp(slope, surface.soilSlopeStart(), surface.soilSlopeFull())) {
            return new Column(height, surface.filler(), surface.filler(), depth);
        }
        return new Column(height, surface.top(), surface.filler(), depth);
    }

    // Patchy noise roughened by per-block speckle, in [0, 1]
    private double transitionRoll(int x, int z, TerrainNoise noise) {
        double patches = 0.5 + 0.5 * noise.fbm(NoiseLayer.PATCHES, new Vector2d(x, z), surface.patchScale(), 2);
        double roll = (1.0 - surface.grit()) * patches + surface.grit() * noise.hash(x, z);
        return Math.max(0.0, Math.min(1.0, roll));
    }

    // 0 below start, 1 at or above full, linear between
    private static double ramp(double value, int start, int full) {
        if (value < start) return 0.0;
        if (value >= full) return 1.0;
        return (value - start) / (full - start);
    }

    @Override
    public String toString() {
        return name;
    }
}
