package Core;

import World.World;
import org.joml.Vector3f;
import org.joml.Vector3i;
import World.ChunkManager;

public class Raycast {
    private final float MAX_DISTANCE = 10f;
    private final float STEP = 0.01f;

    private Vector3f origin;
    private Vector3f direction;
    private final float magnitude;

    private ChunkManager chunkManager;

    public Raycast(World world, float magnitude) {
        this.chunkManager = world.getChunkManager();
        this.magnitude = magnitude;
    }

    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }

    public void setDirection(Vector3f direction){
        this.direction = direction;
    }

    public Vector3i getBlockPosition() {
        Vector3f step = new Vector3f(direction).normalize().mul(STEP);
        Vector3f current = new Vector3f(origin).add(new Vector3f(direction).normalize().mul(0.1f));

        for (float distance = 0.5f; distance < MAX_DISTANCE; distance += STEP) {
            Vector3i blockPos = worldToBlock(current);
            if (chunkManager.isBlockAt(blockPos)) {
                return blockPos;
            }
            current.add(step);
        }
        return null;
    }

    private Vector3i worldToBlock(Vector3f pos) {
        return new Vector3i(
                Math.round(pos.x),
                Math.round(pos.y),
                Math.round(pos.z)
        );
    }
}