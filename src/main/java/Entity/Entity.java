package Entity;

import Core.Mesh;
import Core.Transform;
import World.Chunk;
import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity {
    private static final float GRAVITY_CONSTANT = 32f;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id = NEXT_ID.getAndIncrement();
    private boolean removed = false;
    protected Transform transform;
    protected Vector3f velocity;
    protected AABB boundingBox;

    protected float lastDt;

    protected Mesh mesh;

    protected boolean onGround = false;

    // Longest distance moved along one axis before checking for collisions, so fast entities cannot skip through blocks
    private static final float MAX_COLLISION_STEP = 0.25f;

    protected Entity(Transform transform) {
        this.transform = new Transform(new Vector3f(transform.position), new Vector3f(transform.scale));
        this.velocity = new Vector3f(0, 0, 0);
        this.boundingBox = new AABB(this.transform.position, this.transform.scale.x/2, this.transform.scale.y/2, this.transform.scale.z/2);
    }

    public void update(float dt, ChunkManager chunkManager) {
        this.lastDt = dt;

        Vector3i currentChunk = new Vector3i(
                Math.floorDiv((int) Math.floor(transform.position.x), Chunk.CHUNK_SIZE),
                0,
                Math.floorDiv((int) Math.floor(transform.position.z), Chunk.CHUNK_SIZE)
        );
        if (chunkManager.getChunk(currentChunk) == null) {
            velocity.set(0, 0, 0);
            return;
        }

        applyGravity(dt);
        applyCollisions(chunkManager);
    }

    // Moves the entity by the given displacement, stopping at blocks in the way
    protected void move(Vector3f displacement, ChunkManager chunkManager) {
        moveAndCollide(new Vector3f(displacement), chunkManager);
    }

    // Moves the entity by its velocity over the last frame, stopping at blocks in the way
    protected void applyCollisions(ChunkManager chunkManager) {
        moveAndCollide(new Vector3f(velocity).mul(lastDt), chunkManager);
    }

    private void moveAndCollide(Vector3f displacement, ChunkManager chunkManager) {
        if (chunkManager == null) return;
        onGround = false;

        moveAlongAxis(0, displacement.x, chunkManager);
        moveAlongAxis(1, displacement.y, chunkManager);
        moveAlongAxis(2, displacement.z, chunkManager);

        if (!onGround) {
            int x = (int) Math.floor(transform.position.x + 0.5f);
            int y = (int) Math.floor(transform.position.y - boundingBox.getHalfExtents().y - 0.05f + 0.5f);
            int z = (int) Math.floor(transform.position.z + 0.5f);
            if (chunkManager.isBlockAt(new Vector3i(x, y, z)))
                onGround = true;
        }
    }

    // Moves along one axis (0 = x, 1 = y, 2 = z) in small steps, snapping against the first block hit
    private void moveAlongAxis(int axis, float amount, ChunkManager chunkManager) {
        if (amount == 0) return;
        int steps = (int) Math.ceil(Math.abs(amount) / MAX_COLLISION_STEP);
        float step = amount / steps;

        for (int i = 0; i < steps; i++) {
            transform.position.setComponent(axis, transform.position.get(axis) + step);
            boundingBox.setPosition(transform.position);
            if (resolveBlockCollision(axis, step, chunkManager)) return;
        }
    }

    // Pushes the entity out of any block it overlaps on this axis. Returns true if it hit one
    private boolean resolveBlockCollision(int axis, float direction, ChunkManager chunkManager) {
        boolean hit = false;
        for (Vector3i block : getOverlappingBlocks()) {
            if (!chunkManager.isBlockAt(block)) continue;
            AABB blockAABB = AABB.blockAABB(block.x, block.y, block.z);
            if (!boundingBox.intersects(blockAABB)) continue;

            float halfExtent = boundingBox.getHalfExtents().get(axis);
            if (direction > 0) {
                transform.position.setComponent(axis, blockAABB.getMin().get(axis) - halfExtent);
            } else {
                transform.position.setComponent(axis, blockAABB.getMax().get(axis) + halfExtent);
                if (axis == 1) onGround = true;
            }
            velocity.setComponent(axis, 0);
            boundingBox.setPosition(transform.position);
            hit = true;
        }
        return hit;
    }

    private ArrayList<Vector3i> getOverlappingBlocks() {
        ArrayList<Vector3i> blocks = new ArrayList<>();
        int minX = (int) Math.floor(boundingBox.getMin().x + 0.5f);
        int minY = (int) Math.floor(boundingBox.getMin().y + 0.5f);
        int minZ = (int) Math.floor(boundingBox.getMin().z + 0.5f);
        int maxX = (int) Math.floor(boundingBox.getMax().x + 0.5f);
        int maxY = (int) Math.floor(boundingBox.getMax().y + 0.5f);
        int maxZ = (int) Math.floor(boundingBox.getMax().z + 0.5f);

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Vector3i(x, y, z));
                }

        return blocks;
    }

    protected void applyGravity(float dt){
        if (!onGround){
            velocity.y -= GRAVITY_CONSTANT * dt;
        }
    }

    public void render(){
        if (mesh != null) mesh.draw();
    }

    public Vector3f getPosition() {
        return new Vector3f(transform.position);
    }

    public void setPosition(Vector3f position){
        transform.position.set(position);
        boundingBox.setPosition(transform.position);
        velocity.set(0, 0, 0);
        onGround = false;
    }

    public void cleanup(){
        if (mesh != null) mesh.cleanup();
    }

    // Unique for every entity created while the game runs
    public int getId() {
        return id;
    }

    // Marks the entity to be taken out of the world by its EntityManager
    public void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public Vector3f getVelocity() {
        return velocity;
    }
}
