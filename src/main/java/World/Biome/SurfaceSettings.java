package World.Biome;

import World.Block;

/*
 * Which blocks a biome is made of and when its ground gives way to rock or snow.
 * The defaults suit grassland, so a biome only overrides what differs.
 *
 * Rock and bare soil don't switch on at a hard line. Each rule has a start and a
 * full value: past start a growing share of columns change, and past full they all
 * have. Which columns change first is decided by patchy noise, so bare ground shows
 * through in clumps that grow together, with stone patches inside bare soil patches.
 *
 *   new SurfaceSettings()
 *       .top(Block.GRASS)
 *       .filler(Block.DIRT, 4)
 *       .bareSoilOnSlopes(2, 5)
 *       .stoneOnSlopes(3, 6)
 *       .bareRockAbove(110, 130)
 */
public class SurfaceSettings {
    public static final int NEVER = Integer.MAX_VALUE;

    private Block top = Block.GRASS;
    private Block filler = Block.DIRT;
    private int fillerDepth = 4;
    private int minFillerDepth = 2;

    private int soilSlopeStart = 2;
    private int soilSlopeFull = 5;
    private int stoneSlopeStart = 3;
    private int stoneSlopeFull = 6;
    private int rockHeightStart = 110;
    private int rockHeightFull = 130;

    private double patchScale = 0.12;
    private double grit = 0.3;

    private int snowLine = 165;
    private double snowLineJitter = 10.0;
    private int cliffSlope = 7;

    // Block on the very top of each column
    public SurfaceSettings top(Block block) {
        this.top = block;
        return this;
    }

    // Blocks under the top before stone starts, depth counts the top block too
    public SurfaceSettings filler(Block block, int depth) {
        this.filler = block;
        this.fillerDepth = depth;
        return this;
    }

    // The filler layer thins towards this depth as slopes approach full stone, so rock pokes through thin soil
    public SurfaceSettings thinSoilOnSlopes(int minDepth) {
        this.minFillerDepth = minDepth;
        return this;
    }

    // Slopes where the top block wears away and the filler (such as dirt) shows
    public SurfaceSettings bareSoilOnSlopes(int start, int full) {
        this.soilSlopeStart = start;
        this.soilSlopeFull = full;
        return this;
    }

    // Slopes where the ground turns to bare stone. Slope is the height difference across a column's neighbours.
    public SurfaceSettings stoneOnSlopes(int start, int full) {
        this.stoneSlopeStart = start;
        this.stoneSlopeFull = full;
        return this;
    }

    // Heights where the ground turns to bare stone, a ragged band rather than a level line
    public SurfaceSettings bareRockAbove(int start, int full) {
        this.rockHeightStart = start;
        this.rockHeightFull = full;
        return this;
    }

    public SurfaceSettings noBareRock() {
        this.rockHeightStart = NEVER;
        this.rockHeightFull = NEVER;
        return this;
    }

    // Size of bare ground clumps (smaller scale = bigger clumps) and how much per-block speckle roughens their edges, 0 to 1
    public SurfaceSettings patchiness(double scale, double grit) {
        this.patchScale = scale;
        this.grit = grit;
        return this;
    }

    // Snow settles above the line (moved up or down by jitter) unless the slope is a cliff
    public SurfaceSettings snowAbove(int height, double jitter, int cliffSlope) {
        this.snowLine = height;
        this.snowLineJitter = jitter;
        this.cliffSlope = cliffSlope;
        return this;
    }

    public SurfaceSettings noSnow() {
        this.snowLine = NEVER;
        return this;
    }

    public Block top() {
        return top;
    }

    public Block filler() {
        return filler;
    }

    public int fillerDepth() {
        return fillerDepth;
    }

    public int minFillerDepth() {
        return minFillerDepth;
    }

    public int soilSlopeStart() {
        return soilSlopeStart;
    }

    public int soilSlopeFull() {
        return soilSlopeFull;
    }

    public int stoneSlopeStart() {
        return stoneSlopeStart;
    }

    public int stoneSlopeFull() {
        return stoneSlopeFull;
    }

    public int rockHeightStart() {
        return rockHeightStart;
    }

    public int rockHeightFull() {
        return rockHeightFull;
    }

    public double patchScale() {
        return patchScale;
    }

    public double grit() {
        return grit;
    }

    public int snowLine() {
        return snowLine;
    }

    public double snowLineJitter() {
        return snowLineJitter;
    }

    public int cliffSlope() {
        return cliffSlope;
    }
}
