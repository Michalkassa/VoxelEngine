package Core;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShadowProjectionTest {

    private static final float RADIUS = 256f;
    private static final float DEPTH = 768f;
    private static final int RESOLUTION = 4096;

    private static final Vector3f SUN = new Vector3f(0.557f, 0.766f, 0.321f).normalize();

    private static Vector4f toClip(ShadowProjection projection, Vector3f world) {
        return projection.getLightSpace().transform(new Vector4f(world, 1f));
    }

    private static ShadowProjection at(Vector3f sun, Vector3f centre) {
        ShadowProjection projection = new ShadowProjection(RADIUS, DEPTH, RESOLUTION);
        projection.update(sun, centre);
        return projection;
    }

    @Test
    void texelSizeCoversTheSquare() {
        assertEquals(2f * RADIUS / RESOLUTION, new ShadowProjection(RADIUS, DEPTH, RESOLUTION).texelSize(), 1e-6f);
    }

    @Test
    void centreLandsInTheMiddleOfTheMap() {
        Vector3f centre = new Vector3f(1234.5f, 80f, -987.25f);
        Vector4f clip = toClip(at(SUN, centre), centre);

        float texelInClip = 2f / RESOLUTION;
        assertEquals(0f, clip.x, texelInClip);
        assertEquals(0f, clip.y, texelInClip);
        assertTrue(clip.z > -1f && clip.z < 1f);
    }

    @Test
    void pointsAlongTheSunsRayShareAShadowMapTexel() {
        Vector3f centre = new Vector3f(40f, 70f, 40f);
        ShadowProjection projection = at(SUN, centre);
        Vector3f ground = new Vector3f(60f, 64f, 20f);
        Vector3f caster = new Vector3f(SUN).mul(150f).add(ground);

        Vector4f groundClip = toClip(projection, ground);
        Vector4f casterClip = toClip(projection, caster);

        assertEquals(groundClip.x, casterClip.x, 1e-4f);
        assertEquals(groundClip.y, casterClip.y, 1e-4f);
        // Nearer the sun means a smaller depth, which is what makes the ground shadowed
        assertTrue(casterClip.z < groundClip.z);
    }

    @Test
    void groundAroundTheCentreAndTallTerrainFitInsideTheMap() {
        Vector3f centre = new Vector3f(0f, 70f, 0f);
        ShadowProjection projection = at(SUN, centre);
        float reach = RADIUS * 0.7f;

        for (float x : new float[] { -reach, reach }) {
            for (float z : new float[] { -reach, reach }) {
                Vector4f ground = toClip(projection, new Vector3f(x, centre.y, z));
                assertTrue(Math.abs(ground.x) <= 1f && Math.abs(ground.y) <= 1f, "outside the map at " + x + "," + z);

                // Anything from the world floor to the build limit is inside the depth range, so it can cast
                for (float y : new float[] { 0f, 255f }) {
                    Vector4f clip = toClip(projection, new Vector3f(x, y, z));
                    assertTrue(Math.abs(clip.z) <= 1f, "outside the depth range at " + x + "," + y + "," + z);
                }
            }
        }
    }

    @Test
    void smallMovesShiftShadowsByWholeTexels() {
        Vector3f fixedPoint = new Vector3f(10f, 64f, 10f);
        Vector4f before = toClip(at(SUN, new Vector3f(0f, 70f, 0f)), fixedPoint);
        Vector4f after = toClip(at(SUN, new Vector3f(0.37f, 70f, 0.11f)), fixedPoint);

        float texelInClip = 2f / RESOLUTION;
        float stepsX = (after.x - before.x) / texelInClip;
        float stepsY = (after.y - before.y) / texelInClip;
        assertEquals(Math.round(stepsX), stepsX, 0.01f);
        assertEquals(Math.round(stepsY), stepsY, 0.01f);
    }

    @Test
    void sunStraightOverheadStillWorks() {
        Vector3f centre = new Vector3f(5f, 70f, 5f);
        Vector4f clip = toClip(at(new Vector3f(0f, 1f, 0f), centre), centre);

        assertFalse(Float.isNaN(clip.x) || Float.isNaN(clip.y) || Float.isNaN(clip.z));
        assertEquals(0f, clip.x, 2f / RESOLUTION);
        assertEquals(0f, clip.y, 2f / RESOLUTION);
    }
}
