package Entity;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AABBTest {

    private static final float EPS = 1e-5f;

    private static void assertVec(float x, float y, float z, Vector3f actual) {
        assertEquals(x, actual.x, EPS);
        assertEquals(y, actual.y, EPS);
        assertEquals(z, actual.z, EPS);
    }

    @Test
    void minMaxConstructorCopiesVectors() {
        Vector3f min = new Vector3f(0, 0, 0);
        Vector3f max = new Vector3f(1, 1, 1);

        AABB box = new AABB(min, max);
        min.set(-5, -5, -5);
        max.set(5, 5, 5);

        assertVec(0, 0, 0, box.getMin());
        assertVec(1, 1, 1, box.getMax());
    }

    @Test
    void centerConstructorBuildsBoundsFromHalfExtents() {
        AABB box = new AABB(new Vector3f(10, 20, 30), 1, 2, 3);

        assertVec(9, 18, 27, box.getMin());
        assertVec(11, 22, 33, box.getMax());
    }

    @Test
    void gettersReturnCopies() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        box.getMin().set(100, 100, 100);
        box.getMax().set(100, 100, 100);

        assertVec(0, 0, 0, box.getMin());
        assertVec(1, 1, 1, box.getMax());
    }

    @Test
    void centerSizeAndHalfExtents() {
        AABB box = new AABB(new Vector3f(-1, 0, 2), new Vector3f(3, 6, 4));

        assertVec(1, 3, 3, box.getCenter());
        assertVec(4, 6, 2, box.getSize());
        assertVec(2, 3, 1, box.getHalfExtents());
    }

    @Test
    void overlappingBoxesIntersect() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(2, 2, 2));
        AABB b = new AABB(new Vector3f(1, 1, 1), new Vector3f(3, 3, 3));

        assertTrue(a.intersects(b));
        assertTrue(b.intersects(a));
    }

    @Test
    void containedBoxIntersects() {
        AABB outer = new AABB(new Vector3f(0, 0, 0), new Vector3f(10, 10, 10));
        AABB inner = new AABB(new Vector3f(4, 4, 4), new Vector3f(5, 5, 5));

        assertTrue(outer.intersects(inner));
        assertTrue(inner.intersects(outer));
    }

    @Test
    void separatedBoxesDoNotIntersect() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        assertFalse(a.intersects(new AABB(new Vector3f(2, 0, 0), new Vector3f(3, 1, 1))));
        assertFalse(a.intersects(new AABB(new Vector3f(0, 2, 0), new Vector3f(1, 3, 1))));
        assertFalse(a.intersects(new AABB(new Vector3f(0, 0, 2), new Vector3f(1, 1, 3))));
    }

    @Test
    void touchingFacesDoNotIntersect() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        AABB b = new AABB(new Vector3f(1, 0, 0), new Vector3f(2, 1, 1));

        assertFalse(a.intersects(b));
    }

    @Test
    void overlapSmallerThanToleranceDoesNotIntersect() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        AABB b = new AABB(new Vector3f(0.9995f, 0, 0), new Vector3f(2, 1, 1));

        assertFalse(a.intersects(b));
    }

    @Test
    void containsPointsInsideAndOnBoundary() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        assertTrue(box.contains(new Vector3f(0.5f, 0.5f, 0.5f)));
        assertTrue(box.contains(new Vector3f(0, 0, 0)));
        assertTrue(box.contains(new Vector3f(1, 1, 1)));
        assertFalse(box.contains(new Vector3f(1.01f, 0.5f, 0.5f)));
        assertFalse(box.contains(new Vector3f(0.5f, -0.01f, 0.5f)));
    }

    @Test
    void offsetReturnsMovedCopy() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        AABB moved = box.offset(new Vector3f(2, 3, 4));

        assertVec(2, 3, 4, moved.getMin());
        assertVec(3, 4, 5, moved.getMax());
        assertVec(0, 0, 0, box.getMin());
    }

    @Test
    void uniformExpandGrowsAllSides() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        AABB grown = box.expand(0.5f);

        assertVec(-0.5f, -0.5f, -0.5f, grown.getMin());
        assertVec(1.5f, 1.5f, 1.5f, grown.getMax());
        assertVec(1, 1, 1, box.getMax());
    }

    @Test
    void perAxisExpandGrowsEachAxis() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

        AABB grown = box.expand(1, 2, 3);

        assertVec(-1, -2, -3, grown.getMin());
        assertVec(2, 3, 4, grown.getMax());
    }

    @Test
    void settersCopyVectors() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        Vector3f min = new Vector3f(-1, -1, -1);
        Vector3f max = new Vector3f(2, 2, 2);

        box.setMin(min);
        box.setMax(max);
        min.set(9, 9, 9);
        max.set(9, 9, 9);

        assertVec(-1, -1, -1, box.getMin());
        assertVec(2, 2, 2, box.getMax());
    }

    @Test
    void setPositionRecentersKeepingSize() {
        AABB box = new AABB(new Vector3f(0, 0, 0), new Vector3f(2, 4, 6));

        box.setPosition(new Vector3f(10, 10, 10));

        assertVec(10, 10, 10, box.getCenter());
        assertVec(2, 4, 6, box.getSize());
        assertVec(9, 8, 7, box.getMin());
    }

    @Test
    void penetrationIsNullWhenNotIntersecting() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
        AABB b = new AABB(new Vector3f(5, 5, 5), new Vector3f(6, 6, 6));

        assertNull(a.getPenetration(b));
    }

    @Test
    void penetrationPicksSmallestOverlapPerAxis() {
        AABB a = new AABB(new Vector3f(0, 0, 0), new Vector3f(2, 2, 2));
        AABB b = new AABB(new Vector3f(1.5f, 0.5f, -1.75f), new Vector3f(3, 1.5f, 0.25f));

        Vector3f penetration = a.getPenetration(b);

        assertNotNull(penetration);
        assertVec(-0.5f, 1.5f, 0.25f, penetration);
    }

    @Test
    void blockAABBIsUnitCubeCenteredOnBlock() {
        AABB block = AABB.blockAABB(3, -2, 7);

        assertVec(2.5f, -2.5f, 6.5f, block.getMin());
        assertVec(3.5f, -1.5f, 7.5f, block.getMax());
    }

    @Test
    void toStringIncludesBounds() {
        String text = new AABB(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)).toString();

        assertTrue(text.startsWith("AABB{min="));
        assertTrue(text.contains("max="));
    }
}
