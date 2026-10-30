package World;

import World.Biome.Biome;
import World.Biome.Biomes;
import World.Biome.Climate;
import World.Biome.DesertBiome;
import World.Biome.HillyPlainsBiome;
import World.Biome.MountainsBiome;
import World.Biome.PlainsBiome;
import World.Biome.SurfaceSettings;
import World.Biome.TerrainNoise;
import World.Biome.TerrainSettings;
import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BiomeTest {

    private static final long SEED = 12345;
    private static final TerrainNoise NOISE = new TerrainNoise(SEED);

    // Flat ground at a fixed height, made of whatever blocks the test asks for
    private static class FlatBiome extends Biome {
        FlatBiome(Climate climate, double height, SurfaceSettings surface) {
            super("Flat", climate, new TerrainSettings().baseHeight(height), surface);
        }
    }

    @Test
    void climateDistanceIsEuclidean() {
        Climate a = new Climate(0.0, 0.0, 0.0);
        Climate b = new Climate(0.3, 0.4, 0.0);

        assertEquals(0.5, a.distanceTo(b), 1e-9);
        assertEquals(0.5, b.distanceTo(a), 1e-9);
        assertEquals(0.0, a.distanceTo(a), 1e-9);
    }

    @Test
    void registeredBiomesHaveUniqueNamesAndClimates() {
        Set<String> names = new HashSet<>();
        Set<Climate> climates = new HashSet<>();
        for (Biome biome : Biomes.ALL) {
            assertTrue(names.add(biome.getName()), "duplicate biome name " + biome.getName());
            assertTrue(climates.add(biome.getClimate()), biome.getName() + " shares a climate with another biome");
        }
    }

    @Test
    void biomeWithoutHillsOrShapeIsFlatAtItsBaseHeight() {
        Biome flat = new FlatBiome(new Climate(0, 0, 0), 70, new SurfaceSettings());
        TerrainNoise noise = new TerrainNoise(SEED);

        for (int i = 0; i < 50; i++) {
            assertEquals(70.0, flat.height(new Vector2d(i * 97, i * -53), noise), 1e-9);
        }
    }

    @Test
    void surfaceUsesConfiguredTopAndFiller() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64,
                new SurfaceSettings().top(Block.SAND).filler(Block.DIRT, 3));
        TerrainGenerator.Column column = biome.surface(0, 0, 64, 0, 0.0, NOISE);

        assertEquals(Block.SAND, column.blockAtDepth(0));
        assertEquals(Block.DIRT, column.blockAtDepth(1));
        assertEquals(Block.DIRT, column.blockAtDepth(2));
        assertEquals(Block.STONE, column.blockAtDepth(3));
    }

    @Test
    void steepSlopesAndHighGroundTurnToStone() {
        Biome plains = new PlainsBiome();

        // Plains turn to stone from slope 3 (fully by 6) and from height 110 (fully by 130)
        assertEquals(1.0, shareOf(plains, Block.STONE, 70, 6));
        assertEquals(0.0, shareOf(plains, Block.STONE, 70, 2));
        assertEquals(1.0, shareOf(plains, Block.STONE, 130, 0));
        assertEquals(0.0, shareOf(plains, Block.STONE, 109, 0));
        assertEquals(1.0, shareOf(plains, Block.GRASS, 70, 0));
    }

    @Test
    void snowSettlesAboveTheSnowLineButNotOnCliffs() {
        Biome mountains = new MountainsBiome();

        assertEquals(Block.SNOW, mountains.surface(0, 0, 180, 2, 0.0, NOISE).surface());
        assertEquals(Block.STONE, mountains.surface(0, 0, 180, 7, 0.0, NOISE).surface());
        assertEquals(Block.STONE, mountains.surface(0, 0, 150, 2, 0.0, NOISE).surface());
    }

    @Test
    void snowLineMovesWithDetailNoise() {
        Biome mountains = new MountainsBiome();

        assertEquals(Block.SNOW, mountains.surface(0, 0, 160, 0, -1.0, NOISE).surface());
        assertEquals(Block.STONE, mountains.surface(0, 0, 170, 0, 1.0, NOISE).surface());
    }

    @Test
    void desertNeverGetsSnow() {
        Biome desert = new DesertBiome();

        assertEquals(Block.STONE, desert.surface(0, 0, 200, 0, 0.0, NOISE).surface());
        assertEquals(Block.SAND, desert.surface(0, 0, 64, 0, 0.0, NOISE).surface());
    }

    @Test
    void hillyPlainsSitBetweenPlainsAndMountains() {
        TerrainNoise noise = new TerrainNoise(SEED);
        Biome plains = new PlainsBiome();
        Biome hilly = new HillyPlainsBiome();
        Biome mountains = new MountainsBiome();
        double plainsTotal = 0, hillyTotal = 0, mountainsTotal = 0;

        for (int i = 0; i < 400; i++) {
            Vector2d p = new Vector2d(i * 131, i * -89);
            plainsTotal += plains.height(p, noise);
            hillyTotal += hilly.height(p, noise);
            mountainsTotal += mountains.height(p, noise);
        }

        assertTrue(hillyTotal > plainsTotal, "hilly plains average below plains");
        assertTrue(hillyTotal < mountainsTotal, "hilly plains average above mountains");
    }

    private static double shareOf(Biome biome, Block block, int height, int slope) {
        int matches = 0, total = 0;
        for (int x = 0; x < 200; x++) {
            for (int z = 0; z < 200; z++) {
                total++;
                if (biome.surface(x, z, height, slope, 0.0, NOISE).surface() == block) matches++;
            }
        }
        return (double) matches / total;
    }

    @Test
    void stoneOnSlopesFadesInBetweenStartAndFull() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64, new SurfaceSettings().stoneOnSlopes(2, 8).noBareRock());

        double previous = -1;
        for (int slope = 0; slope <= 9; slope++) {
            double stone = shareOf(biome, Block.STONE, 64, slope);
            if (slope <= 2) assertEquals(0.0, stone, "stone at slope " + slope);
            if (slope >= 8) assertEquals(1.0, stone, "grass left at slope " + slope);
            assertTrue(stone >= previous, "stone share fell at slope " + slope);
            previous = stone;
        }
        double halfway = shareOf(biome, Block.STONE, 64, 5);
        assertTrue(halfway > 0.2 && halfway < 0.8, "halfway slope is " + (100 * halfway) + "% stone");
    }

    @Test
    void bareRockFadesInBetweenStartAndFullHeight() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64, new SurfaceSettings().bareRockAbove(100, 140));

        assertEquals(0.0, shareOf(biome, Block.STONE, 99, 0));
        assertEquals(1.0, shareOf(biome, Block.STONE, 140, 0));
        double halfway = shareOf(biome, Block.STONE, 120, 0);
        assertTrue(halfway > 0.2 && halfway < 0.8, "halfway height is " + (100 * halfway) + "% stone");
    }

    @Test
    void bareSoilShowsOnModerateSlopesBeforeStone() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64,
                new SurfaceSettings().bareSoilOnSlopes(1, 4).stoneOnSlopes(5, 8).noBareRock());

        assertEquals(0.0, shareOf(biome, Block.DIRT, 64, 1));
        assertEquals(1.0, shareOf(biome, Block.DIRT, 64, 4));
        double partial = shareOf(biome, Block.DIRT, 64, 2);
        assertTrue(partial > 0.0 && partial < 1.0, "slope 2 is " + (100 * partial) + "% bare dirt");
    }

    @Test
    void stonePatchesSitInsideBareSoilPatches() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64,
                new SurfaceSettings().bareSoilOnSlopes(2, 6).stoneOnSlopes(3, 7).noBareRock());

        for (int x = 0; x < 200; x++) {
            for (int z = 0; z < 200; z++) {
                // A column that is stone at a gentler slope is never grass at a steeper one
                if (biome.surface(x, z, 64, 4, 0.0, NOISE).surface() == Block.STONE) {
                    assertNotEquals(Block.GRASS, biome.surface(x, z, 64, 5, 0.0, NOISE).surface());
                }
                // Same for bare soil
                if (biome.surface(x, z, 64, 3, 0.0, NOISE).surface() == Block.DIRT) {
                    assertNotEquals(Block.GRASS, biome.surface(x, z, 64, 4, 0.0, NOISE).surface());
                }
            }
        }
    }

    @Test
    void soilThinsOnSteepSlopes() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64,
                new SurfaceSettings().filler(Block.DIRT, 5).thinSoilOnSlopes(2).stoneOnSlopes(20, 21).bareSoilOnSlopes(20, 21));

        assertEquals(5, biome.surface(0, 0, 64, 0, 0.0, NOISE).surfaceDepth());
        assertEquals(2, biome.surface(0, 0, 64, 20, 0.0, NOISE).surfaceDepth());
        int middle = biome.surface(0, 0, 64, 10, 0.0, NOISE).surfaceDepth();
        assertTrue(middle > 2 && middle < 5, "depth halfway is " + middle);
    }

    @Test
    void surfaceIsTheSameEveryTimeForAColumn() {
        Biome biome = new MountainsBiome();
        TerrainNoise other = new TerrainNoise(SEED);

        for (int x = 0; x < 100; x++) {
            assertEquals(biome.surface(x, -x * 3, 125, 4, 0.0, NOISE), biome.surface(x, -x * 3, 125, 4, 0.0, other));
        }
    }

    @Test
    void transitionBlocksFormPatchesNotStatic() {
        Biome biome = new FlatBiome(new Climate(0, 0, 0), 64, new SurfaceSettings().stoneOnSlopes(0, 10).noBareRock());
        int stone = 0, sameAsNeighbour = 0, pairs = 0;

        for (int x = 0; x < 200; x++) {
            for (int z = 0; z < 200; z++) {
                boolean here = biome.surface(x, z, 64, 5, 0.0, NOISE).surface() == Block.STONE;
                boolean next = biome.surface(x + 1, z, 64, 5, 0.0, NOISE).surface() == Block.STONE;
                if (here) stone++;
                if (here == next) sameAsNeighbour++;
                pairs++;
            }
        }

        double share = (double) stone / pairs;
        // Independent coin flips at this share would match their neighbour about 1 - 2p(1-p) of the time
        double randomMatch = 1 - 2 * share * (1 - share);
        assertTrue((double) sameAsNeighbour / pairs > randomMatch + 0.15,
                "neighbours match " + (100.0 * sameAsNeighbour / pairs) + "%, static would match " + (100 * randomMatch) + "%");
    }

    @Test
    void generatorRejectsEmptyBiomeList() {
        assertThrows(IllegalArgumentException.class, () -> new TerrainGenerator(SEED, List.of()));
    }

    @Test
    void singleBiomeWorldUsesOnlyThatBiome() {
        Biome flat = new FlatBiome(new Climate(0, 0, 0), 64, new SurfaceSettings().top(Block.SAND).filler(Block.SAND, 2));
        TerrainGenerator generator = new TerrainGenerator(SEED, List.of(flat));

        for (int x = -2000; x <= 2000; x += 100) {
            for (int z = -2000; z <= 2000; z += 100) {
                assertSame(flat, generator.getBiome(x, z));
                assertEquals(Block.SAND, generator.describeColumn(x, z, 64, 0).surface());
            }
        }
    }

    @Test
    void biomeBaseHeightRaisesTheGround() {
        Climate climate = new Climate(0, 0, 0);
        TerrainGenerator low = new TerrainGenerator(SEED, List.of(new FlatBiome(climate, 50, new SurfaceSettings())));
        TerrainGenerator high = new TerrainGenerator(SEED, List.of(new FlatBiome(climate, 80, new SurfaceSettings())));

        for (int x = -1000; x <= 1000; x += 125) {
            assertEquals(30, high.getHeight(x, x / 2) - low.getHeight(x, x / 2));
        }
    }

    @Test
    void everyRegisteredBiomeAppearsInALargeWorld() {
        TerrainGenerator generator = new TerrainGenerator(SEED);
        Map<Biome, Integer> counts = new HashMap<>();
        int total = 0;

        for (int x = -6000; x <= 6000; x += 80) {
            for (int z = -6000; z <= 6000; z += 80) {
                counts.merge(generator.getBiome(x, z), 1, Integer::sum);
                total++;
            }
        }

        for (Biome biome : Biomes.ALL) {
            int count = counts.getOrDefault(biome, 0);
            assertTrue(count > total * 0.03, biome.getName() + " only covers " + (100.0 * count / total) + "%");
        }
    }

    @Test
    void biomeBordersBlendWithoutCliffs() {
        TerrainGenerator generator = new TerrainGenerator(SEED);
        int borders = 0;

        for (int z = -3000; z <= 3000; z += 150) {
            Biome previous = generator.getBiome(-3000, z);
            for (int x = -2999; x <= 3000; x++) {
                Biome current = generator.getBiome(x, z);
                if (current == previous) continue;
                borders++;
                // Mountain borders sit on foothills that are still climbing
                int limit = current instanceof MountainsBiome || previous instanceof MountainsBiome ? 6 : 3;
                int step = Math.abs(generator.getHeight(x, z) - generator.getHeight(x - 1, z));
                assertTrue(step <= limit, previous + " -> " + current + " jumps " + step + " at " + x + "," + z);
                previous = current;
            }
        }

        assertTrue(borders > 20, "only crossed " + borders + " biome borders");
    }

    @Test
    void sandAppearsInDesertsAndGrassInPlains() {
        TerrainGenerator generator = new TerrainGenerator(SEED);
        int desertSand = 0, desertTotal = 0, plainsGrass = 0, plainsTotal = 0, shoreSand = 0, shoreTotal = 0;

        for (int x = -4000; x <= 4000; x += 50) {
            for (int z = -4000; z <= 4000; z += 50) {
                Biome biome = generator.getBiome(x, z);
                int height = generator.getHeight(x, z);
                Block top = generator.describeColumn(x, z, height, 0).surface();
                // Flat ground at or just above the waterline is beach or sea floor whatever the biome
                if (height <= TerrainGenerator.SEA_LEVEL + 2) {
                    shoreTotal++;
                    if (top == Block.SAND) shoreSand++;
                } else if (biome instanceof DesertBiome) {
                    desertTotal++;
                    if (top == Block.SAND) desertSand++;
                } else if (biome instanceof PlainsBiome) {
                    plainsTotal++;
                    if (top == Block.GRASS) plainsGrass++;
                }
            }
        }

        // Edges are jittered, so a few border columns take their neighbour's blocks
        assertTrue(desertSand > desertTotal * 0.9, desertSand + " of " + desertTotal + " desert columns are sand");
        assertTrue(plainsGrass > plainsTotal * 0.9, plainsGrass + " of " + plainsTotal + " plains columns are grass");
        assertTrue(shoreTotal > 0, "no shore columns sampled");
        assertEquals(shoreTotal, shoreSand, shoreSand + " of " + shoreTotal + " shore columns are sand");
    }
}
