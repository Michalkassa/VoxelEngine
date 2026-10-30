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

    // Picks the blocks of a column. detail is a small noise value in [-1, 1] for breaking up hard lines.
    public Column surface(int height, int slope, double detail) {
        if (surface.snowLine() != SurfaceSettings.NEVER) {
            int snowLine = (int) (surface.snowLine() + detail * surface.snowLineJitter());
            if (height >= snowLine && slope < surface.cliffSlope()) {
                return new Column(height, Block.SNOW, Block.SNOW, Math.min(4, 1 + (height - snowLine) / 10));
            }
        }
        if (height >= surface.rockHeight() || slope >= surface.steepSlope()) {
            return new Column(height, Block.STONE, Block.STONE, 0);
        }
        return new Column(height, surface.top(), surface.filler(), surface.fillerDepth());
    }

    @Override
    public String toString() {
        return name;
    }
}
