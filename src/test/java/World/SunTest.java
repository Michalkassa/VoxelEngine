package World;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SunTest {

    private static Vector3f direction(float elevation, float azimuth) {
        return new Sun(elevation, azimuth).getDirection(new Vector3f());
    }

    @Test
    void sunOverheadPointsStraightUp() {
        Vector3f direction = direction(90f, 0f);

        assertEquals(0f, direction.x, 1e-6f);
        assertEquals(1f, direction.y, 1e-6f);
        assertEquals(0f, direction.z, 1e-6f);
    }

    @Test
    void sunOnHorizonPointsAlongAzimuth() {
        Vector3f east = direction(0f, 0f);
        assertEquals(1f, east.x, 1e-6f);
        assertEquals(0f, east.y, 1e-6f);

        Vector3f south = direction(0f, 90f);
        assertEquals(0f, south.x, 1e-6f);
        assertEquals(1f, south.z, 1e-6f);
    }

    @Test
    void directionIsAlwaysUnitLength() {
        for (float elevation = -90f; elevation <= 90f; elevation += 15f) {
            for (float azimuth = 0f; azimuth < 360f; azimuth += 30f) {
                assertEquals(1f, direction(elevation, azimuth).length(), 1e-5f);
            }
        }
    }

    @Test
    void movingTheSunChangesItsDirection() {
        Sun sun = new Sun(50f, 30f);
        Vector3f before = sun.getDirection(new Vector3f());

        sun.setPosition(20f, 200f);

        assertEquals(20f, sun.getElevation());
        assertEquals(200f, sun.getAzimuth());
        assertNotEquals(before, sun.getDirection(new Vector3f()));
    }
}
