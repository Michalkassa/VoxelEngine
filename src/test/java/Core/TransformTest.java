package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransformTest {

    private static final float EPS = 1e-5f;

    @Test
    void constructorKeepsPositionAndScaleReferences() {
        Vector3f position = new Vector3f(1, 2, 3);
        Vector3f scale = new Vector3f(4, 5, 6);

        Transform transform = new Transform(position, scale);

        assertSame(position, transform.position);
        assertSame(scale, transform.scale);
        assertEquals(new Vector3f(0, 0, 0), transform.rotation);
    }

    @Test
    void identityTransformProducesIdentityMatrix() {
        Transform transform = new Transform(new Vector3f(), new Vector3f(1, 1, 1));

        assertTrue(transform.getMatrix().equals(new Matrix4f(), EPS));
    }

    @Test
    void matrixTranslatesPoints() {
        Transform transform = new Transform(new Vector3f(10, -5, 2), new Vector3f(1, 1, 1));

        Vector3f point = transform.getMatrix().transformPosition(new Vector3f(1, 1, 1));

        assertEquals(11, point.x, EPS);
        assertEquals(-4, point.y, EPS);
        assertEquals(3, point.z, EPS);
    }

    @Test
    void matrixScalesBeforeTranslating() {
        Transform transform = new Transform(new Vector3f(1, 0, 0), new Vector3f(2, 3, 4));

        Vector3f point = transform.getMatrix().transformPosition(new Vector3f(1, 1, 1));

        assertEquals(3, point.x, EPS);
        assertEquals(3, point.y, EPS);
        assertEquals(4, point.z, EPS);
    }

    @Test
    void matrixAppliesRotation() {
        Transform transform = new Transform(new Vector3f(), new Vector3f(1, 1, 1));
        transform.rotation.set(0, (float) Math.toRadians(90), 0);

        Vector3f point = transform.getMatrix().transformPosition(new Vector3f(1, 0, 0));

        assertEquals(0, point.x, EPS);
        assertEquals(0, point.y, EPS);
        assertEquals(-1, point.z, EPS);
    }

    @Test
    void matrixReflectsLaterChangesToPosition() {
        Vector3f position = new Vector3f();
        Transform transform = new Transform(position, new Vector3f(1, 1, 1));

        position.set(0, 7, 0);

        assertEquals(7, transform.getMatrix().transformPosition(new Vector3f()).y, EPS);
    }
}
