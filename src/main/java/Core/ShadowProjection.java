package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;

// Works out the sun's view and orthographic projection for the shadow map: a square of the world centred on a point,
// seen straight down the sun's direction
public class ShadowProjection {

    private final float radius;
    private final float depth;
    private final int resolution;

    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f lightSpace = new Matrix4f();
    private final Vector3f centreInLight = new Vector3f();

    // radius: half the width of the square that gets shadows, in blocks
    // depth: how far towards and away from the sun casters are still caught, in blocks
    public ShadowProjection(float radius, float depth, int resolution) {
        this.radius = radius;
        this.depth = depth;
        this.resolution = resolution;
    }

    public void update(Vector3f sunDirection, Vector3f centre) {
        // The view only depends on the sun's direction, so a moving centre just slides the projection window
        Vector3f up = Math.abs(sunDirection.y) > 0.99f ? new Vector3f(0, 0, 1) : new Vector3f(0, 1, 0);
        view.setLookAt(sunDirection.x, sunDirection.y, sunDirection.z, 0, 0, 0, up.x, up.y, up.z);

        // Snap the window to whole shadow map texels, otherwise shadow edges crawl as the player moves
        view.transformPosition(centre, centreInLight);
        float texel = texelSize();
        float x = (float) Math.floor(centreInLight.x / texel) * texel;
        float y = (float) Math.floor(centreInLight.y / texel) * texel;
        float distance = -centreInLight.z;

        projection.setOrtho(x - radius, x + radius, y - radius, y + radius, distance - depth, distance + depth);
        projection.mul(view, lightSpace);
    }

    // Width of one shadow map texel, in blocks
    public float texelSize() {
        return 2f * radius / resolution;
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    // Projection times view: takes a world position into the shadow map's clip space
    public Matrix4f getLightSpace() {
        return lightSpace;
    }

    public int getResolution() {
        return resolution;
    }
}
