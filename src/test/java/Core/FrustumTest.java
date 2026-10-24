package Core;

import org.joml.Matrix4f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrustumTest {

    private Frustum frustum;

    @BeforeEach
    void setUp() {
        frustum = new Frustum();
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(90), 1f, 0.1f, 100f);
        Matrix4f view = new Matrix4f().lookAt(0, 0, 0, 0, 0, -1, 0, 1, 0);
        frustum.update(projection, view);
    }

    private boolean unitBoxVisibleAt(float x, float y, float z) {
        return frustum.isBoxVisible(x - 0.5f, y - 0.5f, z - 0.5f, x + 0.5f, y + 0.5f, z + 0.5f);
    }

    @Test
    void newFrustumTreatsEverythingAsVisible() {
        Frustum fresh = new Frustum();

        assertTrue(fresh.isBoxVisible(1000, 1000, 1000, 1001, 1001, 1001));
    }

    @Test
    void boxInFrontOfCameraIsVisible() {
        assertTrue(unitBoxVisibleAt(0, 0, -10));
    }

    @Test
    void boxBehindCameraIsNotVisible() {
        assertFalse(unitBoxVisibleAt(0, 0, 10));
    }

    @Test
    void boxBeyondFarPlaneIsNotVisible() {
        assertFalse(unitBoxVisibleAt(0, 0, -200));
    }

    @Test
    void boxCloserThanNearPlaneIsNotVisible() {
        assertFalse(frustum.isBoxVisible(-0.01f, -0.01f, -0.05f, 0.01f, 0.01f, -0.01f));
    }

    @Test
    void boxesOutsideSidePlanesAreNotVisible() {
        assertFalse(unitBoxVisibleAt(20, 0, -10));
        assertFalse(unitBoxVisibleAt(-20, 0, -10));
        assertFalse(unitBoxVisibleAt(0, 20, -10));
        assertFalse(unitBoxVisibleAt(0, -20, -10));
    }

    @Test
    void boxPartiallyInsideIsVisible() {
        assertTrue(frustum.isBoxVisible(5, -1, -11, 30, 1, -9));
    }

    @Test
    void boxSurroundingCameraIsVisible() {
        assertTrue(frustum.isBoxVisible(-50, -50, -50, 50, 50, 50));
    }

    @Test
    void updateFollowsNewViewDirection() {
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(90), 1f, 0.1f, 100f);
        Matrix4f view = new Matrix4f().lookAt(0, 0, 0, 0, 0, 1, 0, 1, 0);

        frustum.update(projection, view);

        assertTrue(unitBoxVisibleAt(0, 0, 10));
        assertFalse(unitBoxVisibleAt(0, 0, -10));
    }
}
