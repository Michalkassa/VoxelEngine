package World.Biome;

import World.Block;

/*
 * Which blocks a biome is made of and when its ground gives way to rock or snow.
 * The defaults are the old world-wide rules, so a biome only overrides what differs.
 *
 *   new SurfaceSettings()
 *       .top(Block.SAND)
 *       .filler(Block.SAND, 5)
 *       .noSnow()
 */
public class SurfaceSettings {
    public static final int NEVER = Integer.MAX_VALUE;

    private Block top = Block.GRASS;
    private Block filler = Block.DIRT;
    private int fillerDepth = 4;
    private int steepSlope = 4;
    private int rockHeight = 120;
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

    // Height difference to a neighbour at which the ground turns to bare stone
    public SurfaceSettings stoneOnSlopesFrom(int slope) {
        this.steepSlope = slope;
        return this;
    }

    // Everything at or above this height is bare stone
    public SurfaceSettings bareRockAbove(int height) {
        this.rockHeight = height;
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

    public int steepSlope() {
        return steepSlope;
    }

    public int rockHeight() {
        return rockHeight;
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
