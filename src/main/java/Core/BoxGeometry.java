package Core;

// Vertices for a 1 x 1 x 1 box centred on the origin, as triangles with outward normals.
// Each vertex is a position followed by a normal, and every face winds counter-clockwise seen from outside
public final class BoxGeometry {

    public static final int FLOATS_PER_VERTEX = 6;
    public static final int VERTEX_COUNT = 36;

    // For each face: its normal, then two edge directions u and v where u x v is the normal
    private static final float[][][] FACES = {
            {{ 1, 0, 0}, {0, 1, 0}, {0, 0, 1}},
            {{-1, 0, 0}, {0, 0, 1}, {0, 1, 0}},
            {{ 0, 1, 0}, {0, 0, 1}, {1, 0, 0}},
            {{ 0,-1, 0}, {1, 0, 0}, {0, 0, 1}},
            {{ 0, 0, 1}, {1, 0, 0}, {0, 1, 0}},
            {{ 0, 0,-1}, {0, 1, 0}, {1, 0, 0}},
    };

    // Corners of a face as steps along u and v, split into two triangles
    private static final int[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, -1}, {1, 1}, {-1, 1}};

    private BoxGeometry() {
    }

    public static float[] vertices() {
        float[] vertices = new float[VERTEX_COUNT * FLOATS_PER_VERTEX];
        int i = 0;
        for (float[][] face : FACES) {
            float[] n = face[0], u = face[1], v = face[2];
            for (int[] corner : CORNERS) {
                for (int axis = 0; axis < 3; axis++) {
                    vertices[i++] = 0.5f * (n[axis] + corner[0] * u[axis] + corner[1] * v[axis]);
                }
                for (int axis = 0; axis < 3; axis++) {
                    vertices[i++] = n[axis];
                }
            }
        }
        return vertices;
    }
}
