package Entity;

import org.joml.Vector3f;

public class AABB {

    private Vector3f min;
    private Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }

    public AABB(Vector3f center, float halfWidth, float halfHeight, float halfDepth) {
        this.min = new Vector3f(
                center.x - halfWidth,
                center.y - halfHeight,
                center.z - halfDepth
        );
        this.max = new Vector3f(
                center.x + halfWidth,
                center.y + halfHeight,
                center.z + halfDepth
        );
    }

    public boolean intersects(AABB other) {
        return (this.min.x <= other.max.x && this.max.x >= other.min.x) &&
                (this.min.y <= other.max.y && this.max.y >= other.min.y) &&
                (this.min.z <= other.max.z && this.max.z >= other.min.z);
    }

    public boolean contains(Vector3f point) {
        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y &&
                point.z >= min.z && point.z <= max.z;
    }

    public Vector3f getCenter() {
        return new Vector3f(
                (min.x + max.x) / 2,
                (min.y + max.y) / 2,
                (min.z + max.z) / 2
        );
    }

    public Vector3f getSize() {
        return new Vector3f(
                max.x - min.x,
                max.y - min.y,
                max.z - min.z
        );
    }

    public Vector3f getHalfExtents() {
        Vector3f size = getSize();
        return new Vector3f(size.x / 2, size.y / 2, size.z / 2);
    }

    public AABB offset(Vector3f offset) {
        return new AABB(
                new Vector3f(min).add(offset),
                new Vector3f(max).add(offset)
        );
    }

    public AABB expand(float amount) {
        return new AABB(
                new Vector3f(min.x - amount, min.y - amount, min.z - amount),
                new Vector3f(max.x + amount, max.y + amount, max.z + amount)
        );
    }

    public AABB expand(float x, float y, float z) {
        return new AABB(
                new Vector3f(min.x - x, min.y - y, min.z - z),
                new Vector3f(max.x + x, max.y + y, max.z + z)
        );
    }

    public Vector3f getMin() {
        return new Vector3f(min);
    }

    public Vector3f getMax() {
        return new Vector3f(max);
    }

    public void setMin(Vector3f min) {
        this.min = new Vector3f(min);
    }

    public void setMax(Vector3f max) {
        this.max = new Vector3f(max);
    }

    public void setPosition(Vector3f center) {
        Vector3f halfExtents = getHalfExtents();
        this.min = new Vector3f(
                center.x - halfExtents.x,
                center.y - halfExtents.y,
                center.z - halfExtents.z
        );
        this.max = new Vector3f(
                center.x + halfExtents.x,
                center.y + halfExtents.y,
                center.z + halfExtents.z
        );
    }

    public Vector3f getPenetration(AABB other) {
        if (!intersects(other)) {
            return null;
        }

        float xOverlap = Math.min(this.max.x - other.min.x, other.max.x - this.min.x);
        float yOverlap = Math.min(this.max.y - other.min.y, other.max.y - this.min.y);
        float zOverlap = Math.min(this.max.z - other.min.z, other.max.z - this.min.z);

        return new Vector3f(xOverlap, yOverlap, zOverlap);
    }

    @Override
    public String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }

    public static AABB blockAABB(int x, int y, int z) {
        return new AABB(
                new Vector3f(x, y, z),
                new Vector3f(x + 1, y + 1, z + 1)
        );
    }
}