package Core;

import World.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import World.ChunkManager;

/*
 * Walks a ray through the block grid one block at a time (Amanatides & Woo voxel traversal),
 * so every block the ray passes through is checked exactly once, in order, and the face it
 * entered the hit block through is known exactly.
 */
public class Raycast {
    private final float MAX_DISTANCE = 10f;

    private Vector3f origin;
    private Vector3f direction;
    private final float magnitude;

    private ChunkManager chunkManager;

    public Raycast(World world, float magnitude) {
        this(world.getChunkManager(), magnitude);
    }

    public Raycast(ChunkManager chunkManager, float magnitude) {
        this.chunkManager = chunkManager;
        this.magnitude = magnitude;
    }

    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }

    public void setDirection(Vector3f direction){
        this.direction = direction;
    }

    // The block the ray hits and the empty block it came from, null when the ray starts inside the block
    private record Hit(Vector3i block, Vector3i previous) {
    }

    private Hit cast() {
        Vector3f dir = new Vector3f(direction).normalize();

        // Blocks are centred on whole coordinates, so shift by half a block to walk a grid whose cells start at whole numbers
        double px = origin.x + 0.5, py = origin.y + 0.5, pz = origin.z + 0.5;
        int x = (int) Math.floor(px), y = (int) Math.floor(py), z = (int) Math.floor(pz);

        int stepX = (int) Math.signum(dir.x), stepY = (int) Math.signum(dir.y), stepZ = (int) Math.signum(dir.z);
        // Distance along the ray to cross one whole block on each axis
        double deltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.x);
        double deltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.y);
        double deltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.z);
        // Distance along the ray to the next block boundary on each axis
        double nextX = boundary(px, x, stepX, deltaX);
        double nextY = boundary(py, y, stepY, deltaY);
        double nextZ = boundary(pz, z, stepZ, deltaZ);

        Vector3i previous = null;
        while (true) {
            Vector3i block = new Vector3i(x, y, z);
            if (chunkManager.isBlockAt(block)) {
                return new Hit(block, previous);
            }
            previous = block;

            if (nextX <= nextY && nextX <= nextZ) {
                if (nextX > MAX_DISTANCE) return null;
                x += stepX;
                nextX += deltaX;
            } else if (nextY <= nextZ) {
                if (nextY > MAX_DISTANCE) return null;
                y += stepY;
                nextY += deltaY;
            } else {
                if (nextZ > MAX_DISTANCE) return null;
                z += stepZ;
                nextZ += deltaZ;
            }
        }
    }

    private static double boundary(double position, int cell, int step, double delta) {
        if (step > 0) return (cell + 1 - position) * delta;
        if (step < 0) return (position - cell) * delta;
        return Double.POSITIVE_INFINITY;
    }

    public Vector3i getBlockPosition() {
        Hit hit = cast();
        return hit == null ? null : hit.block();
    }

    // The empty block against the face the ray hit, where a placed block goes.
    // Each step moves along one axis only, so the block before the hit always shares a face with it.
    public Vector3i getPlacementPosition() {
        Hit hit = cast();
        return hit == null ? null : hit.previous();
    }
}
