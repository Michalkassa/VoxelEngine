package Core;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoxGeometryTest {

    private static final float EPS = 1e-6f;
    private final float[] vertices = BoxGeometry.vertices();

    private Vector3f position(int vertex) {
        int i = vertex * BoxGeometry.FLOATS_PER_VERTEX;
        return new Vector3f(vertices[i], vertices[i + 1], vertices[i + 2]);
    }

    private Vector3f normal(int vertex) {
        int i = vertex * BoxGeometry.FLOATS_PER_VERTEX + 3;
        return new Vector3f(vertices[i], vertices[i + 1], vertices[i + 2]);
    }

    @Test
    void hasTwoTrianglesForEachOfTheSixFaces() {
        assertEquals(36, BoxGeometry.VERTEX_COUNT);
        assertEquals(BoxGeometry.VERTEX_COUNT * BoxGeometry.FLOATS_PER_VERTEX, vertices.length);
    }

    @Test
    void everyCornerIsOnTheUnitBoxAroundTheOrigin() {
        Set<Vector3f> corners = new HashSet<>();
        for (int v = 0; v < BoxGeometry.VERTEX_COUNT; v++) {
            Vector3f p = position(v);
            assertEquals(0.5f, Math.abs(p.x), EPS);
            assertEquals(0.5f, Math.abs(p.y), EPS);
            assertEquals(0.5f, Math.abs(p.z), EPS);
            corners.add(p);
        }
        assertEquals(8, corners.size());
    }

    @Test
    void everyFaceHasADifferentOutwardNormal() {
        Set<Vector3f> normals = new HashSet<>();
        for (int v = 0; v < BoxGeometry.VERTEX_COUNT; v++) {
            Vector3f n = normal(v);
            assertEquals(1f, n.length(), EPS);
            assertTrue(position(v).dot(n) > 0, "normal points into the box");
            normals.add(n);
        }
        assertEquals(6, normals.size());
    }

    // Back-face culling hides triangles that wind clockwise, so every face must wind counter-clockwise from outside
    @Test
    void trianglesWindCounterClockwiseSeenFromOutside() {
        for (int t = 0; t < BoxGeometry.VERTEX_COUNT; t += 3) {
            Vector3f a = position(t), b = position(t + 1), c = position(t + 2);
            Vector3f facing = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));

            assertTrue(facing.dot(normal(t)) > 0, "triangle " + t / 3 + " faces inwards");
            assertEquals(normal(t), normal(t + 1));
            assertEquals(normal(t), normal(t + 2));
        }
    }
}
