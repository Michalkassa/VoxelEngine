package World;

import Core.TextureAtlas;
import World.Biome.Biome;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/*
 * Renders a top-down map of a seed straight from the TerrainGenerator, so it shows
 * exactly the terrain the game would generate. Nothing is stored, every pixel is
 * one column sampled from the generator.
 *
 * Two modes:
 *  - TERRAIN : the top block of each column, coloured like its in-game texture, with hill shading
 *  - BIOMES  : one flat colour per biome, with the same hill shading
 *
 * Each pixel samples a single column, so blocksPerPixel compresses the map: at 4 a pixel stands
 * for a 4 x 4 patch of blocks and the map takes 16 times less work and space than at 1.
 *
 * Run it with: ./gradlew previewMap --args="<seed> [areaInBlocks] [blocksPerPixel] [terrain|biomes] [centerX] [centerZ]"
 */
public class WorldMap {

    public enum Mode { TERRAIN, BIOMES }

    private static final String ATLAS_PATH = "images/texture.png";
    private static final int ATLAS_TILES_WIDE = 4;

    // Light comes from the north-west, this sets how strongly slopes darken or brighten
    private static final double SHADE_STRENGTH = 0.06;
    private static final double MAX_SHADE = 0.35;
    // Deeper water is drawn darker
    private static final double WATER_DEPTH_DARKEN = 0.02;

    private static final int[] BIOME_PALETTE = {
            0x2F5DA8, 0x7DB54A, 0x4E8A3A, 0xE0C77E, 0x8C8C8C,
            0xB4553C, 0x6E4FA0, 0x3FA79C, 0xC9A0DC, 0xD98C2B
    };

    private final TerrainGenerator generator;
    private final Map<Block, Integer> blockColours;

    public WorldMap(TerrainGenerator generator) {
        this.generator = generator;
        this.blockColours = loadBlockColours();
    }

    // Average colour of each block's top texture, so the map matches the game's look
    private static Map<Block, Integer> loadBlockColours() {
        BufferedImage atlas;
        try (InputStream in = WorldMap.class.getClassLoader().getResourceAsStream(ATLAS_PATH)) {
            if (in == null) throw new IllegalStateException("missing texture atlas " + ATLAS_PATH);
            atlas = ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read texture atlas " + ATLAS_PATH, e);
        }

        int tileSize = atlas.getWidth() / ATLAS_TILES_WIDE;
        Map<Block, Integer> colours = new EnumMap<>(Block.class);
        for (TextureAtlas.BlockTextures texture : TextureAtlas.BlockTextures.values()) {
            int tile = TextureAtlas.TEXTURE_ATLAS_RELATIVE_INDEX.get(texture)[0];
            int x0 = (tile % ATLAS_TILES_WIDE) * tileSize;
            int y0 = (tile / ATLAS_TILES_WIDE) * tileSize;

            long r = 0, g = 0, b = 0;
            for (int x = 0; x < tileSize; x++) {
                for (int y = 0; y < tileSize; y++) {
                    int rgb = atlas.getRGB(x0 + x, y0 + y);
                    r += (rgb >> 16) & 0xFF;
                    g += (rgb >> 8) & 0xFF;
                    b += rgb & 0xFF;
                }
            }
            int count = tileSize * tileSize;
            // Block ordinals are offset by one from texture ordinals because AIR has no texture
            colours.put(Block.values()[texture.ordinal() + 1], (int) (r / count) << 16 | (int) (g / count) << 8 | (int) (b / count));
        }
        return colours;
    }

    public int blockColour(Block block) {
        return blockColours.getOrDefault(block, 0xFF00FF);
    }

    public int biomeColour(Biome biome) {
        int index = generator.getBiomes().indexOf(biome);
        return BIOME_PALETTE[Math.floorMod(index, BIOME_PALETTE.length)];
    }

    // Renders an area of widthBlocks x heightBlocks blocks, compressed so each pixel covers blocksPerPixel blocks
    public BufferedImage renderArea(int centerX, int centerZ, int widthBlocks, int heightBlocks, int blocksPerPixel, Mode mode) {
        if (blocksPerPixel <= 0) throw new IllegalArgumentException("blocksPerPixel must be positive");
        return render(centerX, centerZ, pixelsFor(widthBlocks, blocksPerPixel), pixelsFor(heightBlocks, blocksPerPixel), blocksPerPixel, mode);
    }

    // Rounds up so the whole area is covered
    public static int pixelsFor(int blocks, int blocksPerPixel) {
        return (blocks + blocksPerPixel - 1) / blocksPerPixel;
    }

    // Renders width x height pixels centred on (centerX, centerZ), each pixel covering blocksPerPixel blocks.
    // North (negative z) is up.
    public BufferedImage render(int centerX, int centerZ, int width, int height, int blocksPerPixel, Mode mode) {
        if (width <= 0 || height <= 0 || blocksPerPixel <= 0) {
            throw new IllegalArgumentException("map size and scale must be positive");
        }
        int originX = centerX - width * blocksPerPixel / 2;
        int originZ = centerZ - height * blocksPerPixel / 2;

        // Ground heights with a one pixel border so slopes and shading work at the edges
        int[][] heights = new int[width + 2][height + 2];
        IntStream.range(0, width + 2).parallel().forEach(i -> {
            for (int k = 0; k < height + 2; k++) {
                heights[i][k] = generator.getHeight(worldCoord(originX, i - 1, blocksPerPixel), worldCoord(originZ, k - 1, blocksPerPixel));
            }
        });

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, width).parallel().forEach(i -> {
            for (int k = 0; k < height; k++) {
                int x = worldCoord(originX, i, blocksPerPixel);
                int z = worldCoord(originZ, k, blocksPerPixel);
                int ground = heights[i + 1][k + 1];
                // Same slope measure the generator uses, scaled back to blocks
                int slope = Math.max(
                        Math.abs(heights[i + 2][k + 1] - heights[i][k + 1]),
                        Math.abs(heights[i + 1][k + 2] - heights[i + 1][k])) / blocksPerPixel;

                TerrainGenerator.Column column = generator.describeColumn(x, z, ground, slope);
                int colour;
                if (mode == Mode.BIOMES) {
                    colour = biomeColour(generator.getBiome(x, z));
                } else {
                    colour = blockColour(column.blockAtDepth(0));
                }

                double light;
                if (column.waterDepth() > 0) {
                    light = 1.0 - Math.min(0.5, column.waterDepth() * WATER_DEPTH_DARKEN);
                } else {
                    double fall = (heights[i][k] - ground) / (double) blocksPerPixel;
                    light = 1.0 - Math.max(-MAX_SHADE, Math.min(MAX_SHADE, fall * SHADE_STRENGTH));
                }
                image.setRGB(i, k, shade(colour, light));
            }
        });
        return image;
    }

    private static int worldCoord(int origin, int pixel, int blocksPerPixel) {
        return origin + pixel * blocksPerPixel + blocksPerPixel / 2;
    }

    private static int shade(int rgb, double light) {
        int r = (int) Math.max(0, Math.min(255, ((rgb >> 16) & 0xFF) * light));
        int g = (int) Math.max(0, Math.min(255, ((rgb >> 8) & 0xFF) * light));
        int b = (int) Math.max(0, Math.min(255, (rgb & 0xFF) * light));
        return r << 16 | g << 8 | b;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: ./gradlew previewMap --args=\"<seed> [areaInBlocks] [blocksPerPixel] [terrain|biomes] [centerX] [centerZ]\"");
            System.out.println("  areaInBlocks   width and height of the mapped area in blocks (default 4096)");
            System.out.println("  blocksPerPixel compression, how many blocks wide each pixel is (default 4, 1 = every block)");
            return;
        }
        long seed = Long.parseLong(args[0]);
        int area = args.length > 1 ? Integer.parseInt(args[1]) : 4096;
        int blocksPerPixel = args.length > 2 ? Integer.parseInt(args[2]) : 4;
        Mode mode = args.length > 3 ? Mode.valueOf(args[3].toUpperCase()) : Mode.TERRAIN;
        int centerX = args.length > 4 ? Integer.parseInt(args[4]) : 0;
        int centerZ = args.length > 5 ? Integer.parseInt(args[5]) : 0;

        TerrainGenerator generator = new TerrainGenerator(seed);
        WorldMap map = new WorldMap(generator);

        long start = System.currentTimeMillis();
        BufferedImage image = map.renderArea(centerX, centerZ, area, area, blocksPerPixel, mode);
        File out = new File("build/maps/seed-" + seed + "-" + mode.name().toLowerCase() + "-" + area + "blocks-" + blocksPerPixel + "bpp.png");
        out.getParentFile().mkdirs();
        ImageIO.write(image, "png", out);

        System.out.println("Rendered " + area + " x " + area + " blocks around " + centerX + ", " + centerZ
                + " as a " + image.getWidth() + " x " + image.getHeight() + " px image in " + (System.currentTimeMillis() - start) + " ms");
        if (mode == Mode.BIOMES) {
            for (Biome biome : generator.getBiomes()) {
                System.out.printf("  #%06X  %s%n", map.biomeColour(biome), biome.getName());
            }
        }
        System.out.println("Saved " + out.getAbsolutePath() + " (" + out.length() / 1024 + " KB)");
    }
}
