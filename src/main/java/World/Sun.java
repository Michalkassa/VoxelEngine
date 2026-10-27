package World;

import org.joml.Vector3f;

// The sun's position in the sky. Elevation is degrees above the horizon, azimuth is degrees around from +X towards +Z.
public class Sun {

    private float elevation;
    private float azimuth;

    public Sun(float elevation, float azimuth) {
        setPosition(elevation, azimuth);
    }

    public void setPosition(float elevation, float azimuth) {
        this.elevation = elevation;
        this.azimuth = azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public float getAzimuth() {
        return azimuth;
    }

    // Unit vector pointing from the ground towards the sun
    public Vector3f getDirection(Vector3f dest) {
        double elevationRadians = Math.toRadians(elevation);
        double azimuthRadians = Math.toRadians(azimuth);
        double horizontal = Math.cos(elevationRadians);
        return dest.set(
                (float) (horizontal * Math.cos(azimuthRadians)),
                (float) Math.sin(elevationRadians),
                (float) (horizontal * Math.sin(azimuthRadians)));
    }
}
