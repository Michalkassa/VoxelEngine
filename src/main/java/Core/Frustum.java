package Core;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Frustum {
    private final Vector4f[] planes = new Vector4f[6];

    public Frustum() {
        for (int i = 0; i < 6; i++) planes[i] = new Vector4f();
    }

    public void update(Matrix4f projection, Matrix4f view) {
        Matrix4f viewProj = new Matrix4f(projection).mul(view);
        viewProj.frustumPlane(0, planes[0]);
        viewProj.frustumPlane(1, planes[1]);
        viewProj.frustumPlane(2, planes[2]);
        viewProj.frustumPlane(3, planes[3]);
        viewProj.frustumPlane(4, planes[4]);
        viewProj.frustumPlane(5, planes[5]);
    }

    public boolean isBoxVisible(float minX, float minY, float minZ,
                                float maxX, float maxY, float maxZ) {
        for (Vector4f plane : planes) {
            float px = plane.x > 0 ? maxX : minX;
            float py = plane.y > 0 ? maxY : minY;
            float pz = plane.z > 0 ? maxZ : minZ;
            if (plane.x * px + plane.y * py + plane.z * pz + plane.w < 0) return false;
        }
        return true;
    }
}