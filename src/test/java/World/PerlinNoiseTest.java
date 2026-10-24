package World;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PerlinNoiseTest {

    private static final int SAMPLES = 5000;

    private static Vector2d randomPoint(Random random) {
        return new Vector2d(random.nextDouble() * 20000 - 10000, random.nextDouble() * 20000 - 10000);
    }

    @Test
    void sameSeedProducesSameNoise() {
        PerlinNoise a = new PerlinNoise(1234);
        PerlinNoise b = new PerlinNoise(1234);
        Random random = new Random(1);

        for (int i = 0; i < 200; i++) {
            Vector2d p = randomPoint(random);
            assertEquals(a.getOctaveNoise(p), b.getOctaveNoise(p));
            assertEquals(a.getRidgedNoise(p, 0.01, 4, 0.5, 2.0), b.getRidgedNoise(p, 0.01, 4, 0.5, 2.0));
        }
    }

    @Test
    void differentSeedsProduceDifferentNoise() {
        PerlinNoise a = new PerlinNoise(1);
        PerlinNoise b = new PerlinNoise(2);
        Random random = new Random(1);

        int differences = 0;
        for (int i = 0; i < 200; i++) {
            Vector2d p = randomPoint(random);
            if (a.getOctaveNoise(p) != b.getOctaveNoise(p)) differences++;
        }

        assertTrue(differences > 150, "only " + differences + " of 200 samples differed");
    }

    @Test
    void singleOctaveNoiseIsZeroOnIntegerLattice() {
        PerlinNoise noise = new PerlinNoise(99);

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                assertEquals(0f, noise.getPerlinNoiseAtPoint(new Vector2d(x, z)), 1e-9);
            }
        }
    }

    @Test
    void singleOctaveNoiseStaysInRange() {
        PerlinNoise noise = new PerlinNoise(7);
        Random random = new Random(2);

        for (int i = 0; i < SAMPLES; i++) {
            float value = noise.getPerlinNoiseAtPoint(new Vector2d(random.nextDouble() * 500, random.nextDouble() * 500));
            assertTrue(value >= -1f && value <= 1f, "out of range: " + value);
        }
    }

    @Test
    void octaveNoiseStaysInRangeAndIsNotConstant() {
        PerlinNoise noise = new PerlinNoise(7);
        Random random = new Random(3);
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            float value = noise.getOctaveNoise(randomPoint(random), 0.01, 4, 0.5, 2.0);
            assertTrue(value >= -1f && value <= 1f, "out of range: " + value);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        assertTrue(min < -0.1f && max > 0.1f, "range too narrow: " + min + " .. " + max);
    }

    @Test
    void octaveNoiseIsRoughlyCenteredOnZero() {
        PerlinNoise noise = new PerlinNoise(11);
        Random random = new Random(4);
        double sum = 0;

        for (int i = 0; i < SAMPLES; i++) {
            sum += noise.getOctaveNoise(randomPoint(random), 0.01, 4, 0.5, 2.0);
        }

        assertEquals(0.0, sum / SAMPLES, 0.03);
    }

    @Test
    void octaveNoiseIsContinuous() {
        PerlinNoise noise = new PerlinNoise(5);
        Random random = new Random(5);

        for (int i = 0; i < 500; i++) {
            Vector2d p = randomPoint(random);
            float here = noise.getOctaveNoise(p, 0.01, 4, 0.5, 2.0);
            float next = noise.getOctaveNoise(new Vector2d(p.x + 0.01, p.y), 0.01, 4, 0.5, 2.0);
            assertEquals(here, next, 0.01f);
        }
    }

    @Test
    void singleOctaveMatchesPerlinNoiseAtScaledPoint() {
        PerlinNoise noise = new PerlinNoise(8);
        Vector2d p = new Vector2d(123.4, -56.7);

        float octave = noise.getOctaveNoise(p, 0.05, 1, 0.5, 2.0);
        float single = noise.getPerlinNoiseAtPoint(new Vector2d(p.x * 0.05, p.y * 0.05));

        assertEquals(single, octave, 1e-6f);
    }

    @Test
    void defaultOctaveNoiseUsesDefaultParameters() {
        PerlinNoise noise = new PerlinNoise(3);
        Vector2d p = new Vector2d(321, 654);

        assertEquals(
                noise.getOctaveNoise(p, PerlinNoise.DEFAULT_SCALE, PerlinNoise.DEFAULT_OCTAVES,
                        PerlinNoise.DEFAULT_PERSISTENCE, PerlinNoise.DEFAULT_LACUNARITY),
                noise.getOctaveNoise(p));
    }

    @Test
    void ridgedNoiseStaysInZeroToOneAndVaries() {
        PerlinNoise noise = new PerlinNoise(13);
        Random random = new Random(6);
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;

        for (int i = 0; i < SAMPLES; i++) {
            float value = noise.getRidgedNoise(randomPoint(random), 0.006, 5, 0.5, 2.1);
            assertTrue(value >= 0f && value <= 1f, "out of range: " + value);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        assertTrue(min < 0.2f && max > 0.7f, "range too narrow: " + min + " .. " + max);
    }

    @Test
    void ridgedNoisePeaksOnLattice() {
        PerlinNoise noise = new PerlinNoise(21);

        assertEquals(1f, noise.getRidgedNoise(new Vector2d(3, 4), 1.0, 1, 0.5, 2.0), 1e-6f);
    }

    @Test
    void noiseHandlesNegativeCoordinates() {
        PerlinNoise noise = new PerlinNoise(17);

        float value = noise.getOctaveNoise(new Vector2d(-12345.6, -6543.2), 0.01, 4, 0.5, 2.0);

        assertFalse(Float.isNaN(value));
    }
}
