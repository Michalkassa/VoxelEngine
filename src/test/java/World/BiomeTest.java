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
        TerrainGenerator.Column column = biome.surface(64, 0, 0.0);

        assertEquals(Block.SAND, column.blockAtDepth(0));
        assertEquals(Block.DIRT, column.blockAtDepth(1));
        assertEquals(Block.DIRT, column.blockAtDepth(2));
        assertEquals(Block.STONE, column.blockAtDepth(3));
    }

    @Test
    void steepSlopesAndHighGroundTurnToStone() {
        Biome plains = new PlainsBiome();

        assertEquals(Block.STONE, plains.surface(70, 4, 0.0).surface());
        assertEquals(Block.GRASS, plains.surface(70, 3, 0.0).surface());
        assertEquals(Block.STONE, plains.surface(130, 0, 0.0).surface());
    }

    @Test
    void snowSettlesAboveTheSnowLineButNotOnCliffs() {
        Biome mountains = new MountainsBiome();

        assertEquals(Block.SNOW, mountains.surface(180, 2, 0.0).surface());
        assertEquals(Block.STONE, mountains.surface(180, 7, 0.0).surface());
        assertEquals(Block.STONE, mountains.surface(150, 2, 0.0).surface());
    }

    @Test
    void snowLineMovesWithDetailNoise() {
        Biome mountains = new MountainsBiome();

        assertEquals(Block.SNOW, mountains.surface(160, 0, -1.0).surface());
        assertEquals(Block.STONE, mountains.surface(170, 0, 1.0).surface());
    }

    @Test
    void desertNeverGetsSnow() {
        Biome desert = new DesertBiome();

        assertEquals(Block.STONE, desert.surface(200, 0, 0.0).surface());
        assertEquals(Block.SAND, desert.surface(64, 0, 0.0).surface());
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
        int desertSand = 0, desertTotal = 0, plainsGrass = 0, plainsTotal = 0;

        for (int x = -4000; x <= 4000; x += 50) {
            for (int z = -4000; z <= 4000; z += 50) {
                Biome biome = generator.getBiome(x, z);
                Block top = generator.describeColumn(x, z, generator.getHeight(x, z), 0).surface();
                if (biome instanceof DesertBiome) {
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
    }
}
