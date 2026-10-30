package World.Biome;

/*
 * The shared shape every biome starts from: a base level with rolling hills on top.
 * Biomes set these in their constructor and add anything unique in Biome.shape().
 *
 *   new TerrainSettings()
 *       .baseHeight(64)
 *       .hills(12.0, 0.012, 4)
 *       .hillVariation(0.65)
 */
public class TerrainSettings {
    private double baseHeight = 64.0;
    private double hillAmplitude = 0.0;
    private double hillScale = 0.012;
    private int hillOctaves = 4;
    private double hillVariation = 0.0;

    // Average ground level of the biome, before hills and biome-specific shapes
    public TerrainSettings baseHeight(double height) {
        this.baseHeight = height;
        return this;
    }

    // Rolling hills: how tall they get, how wide (smaller scale = wider) and how much fine detail they carry
    public TerrainSettings hills(double amplitude, double scale, int octaves) {
        this.hillAmplitude = amplitude;
        this.hillScale = scale;
        this.hillOctaves = octaves;
        return this;
    }

    // 0 = hills are the same strength everywhere, 1 = some areas flatten out completely
    public TerrainSettings hillVariation(double variation) {
        this.hillVariation = variation;
        return this;
    }

    public double baseHeight() {
        return baseHeight;
    }

    public double hillAmplitude() {
        return hillAmplitude;
    }

    public double hillScale() {
        return hillScale;
    }

    public int hillOctaves() {
        return hillOctaves;
    }

    public double hillVariation() {
        return hillVariation;
    }
}
