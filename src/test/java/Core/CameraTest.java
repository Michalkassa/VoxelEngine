package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private static final float EPS = 1e-4f;

    private static void assertVec(float x, float y, float z, Vector3f actual) {
        assertEquals(x, actual.x, EPS);
        assertEquals(y, actual.y, EPS);
        assertEquals(z, actual.z, EPS);
    }

    private static Camera cameraAt(float x, float y, float z) {
        return new Camera(new Vector3f(x, y, z), new Vector3f());
    }

    @Test
    void constructorStoresPositionAndTarget() {
        Vector3f position = new Vector3f(1, 2, 3);
        Vector3f target = new Vector3f(4, 5, 6);

        Camera camera = new Camera(position, target);

        assertSame(position, camera.position);
        assertSame(target, camera.target);
        assertVec(0, 0, 0, camera.direction);
    }

    @Test
    void defaultFovIsSixtyDegrees() {
        assertEquals(60f, cameraAt(0, 0, 0).getFOV());
    }

    @Test
    void zeroYawAndPitchLooksAlongPositiveX() {
        Camera camera = cameraAt(0, 0, 0);

        camera.update(0.016f);

        assertVec(1, 0, 0, camera.direction);
    }

    @Test
    void yawRotatesDirectionAroundY() {
        Camera camera = cameraAt(0, 0, 0);
        camera.setYaw((float) Math.toRadians(90));

        camera.update(0.016f);

        assertVec(0, 0, 1, camera.direction);
    }

    @Test
    void pitchTiltsDirectionUp() {
        Camera camera = cameraAt(0, 0, 0);
        camera.setPitch((float) Math.toRadians(45));

        camera.update(0.016f);

        float c = (float) Math.cos(Math.toRadians(45));
        assertVec(c, c, 0, camera.direction);
    }

    @Test
    void pitchIsClampedBelowStraightUpAndDown() {
        Camera up = cameraAt(0, 0, 0);
        up.setPitch((float) Math.toRadians(120));
        up.update(0.016f);

        Camera down = cameraAt(0, 0, 0);
        down.setPitch((float) Math.toRadians(-120));
        down.update(0.016f);

        float limit = (float) Math.sin(Math.toRadians(89));
        assertEquals(limit, up.direction.y, EPS);
        assertEquals(-limit, down.direction.y, EPS);
    }

    @Test
    void directionIsUnitLength() {
        Camera camera = cameraAt(0, 0, 0);
        camera.setYaw(1.234f);
        camera.setPitch(-0.567f);

        camera.update(0.016f);

        assertEquals(1f, camera.direction.length(), EPS);
    }

    @Test
    void rightAndUpFormOrthonormalBasis() {
        Camera camera = cameraAt(0, 0, 0);
        camera.setYaw(0.8f);
        camera.setPitch(0.3f);

        camera.update(0.016f);

        assertEquals(1f, camera.right.length(), EPS);
        assertEquals(1f, camera.up.length(), EPS);
        assertEquals(0f, camera.right.dot(camera.direction), EPS);
        assertEquals(0f, camera.up.dot(camera.direction), EPS);
        assertEquals(0f, camera.right.dot(camera.up), EPS);
        assertEquals(0f, camera.right.y, EPS);
        assertTrue(camera.up.y > 0);
    }

    @Test
    void viewMatrixMapsPointAheadOntoNegativeZ() {
        Camera camera = cameraAt(5, 10, 0);
        camera.update(0.016f);

        Matrix4f view = camera.getViewMatrix(new Matrix4f());
        Vector3f ahead = view.transformPosition(new Vector3f(8, 10, 0));

        assertVec(0, 0, -3, ahead);
    }

    @Test
    void viewMatrixWritesIntoDestination() {
        Camera camera = cameraAt(0, 0, 0);
        camera.update(0.016f);
        Matrix4f dest = new Matrix4f();

        assertSame(dest, camera.getViewMatrix(dest));
    }

    @Test
    void setPositionReplacesPosition() {
        Camera camera = cameraAt(0, 0, 0);
        Vector3f position = new Vector3f(7, 8, 9);

        camera.setPosition(position);

        assertSame(position, camera.position);
    }
}
