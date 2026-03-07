package Entity;

import Core.Mesh;
import Core.Transform;
import World.Chunk;
import Core.Game;
import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public abstract class Entity {
    private static final float GRAVITY_CONSTANT = 2 * 9.81f;
    protected Transform transform;
    protected Vector3f velocity;
    protected AABB boundingBox;

    protected float lastDt;

    protected Mesh mesh;

    protected boolean onGround = false;

    protected Entity(Transform transform) {
        this.transform = transform;
        this.velocity = new Vector3f(0, 0, 0);
        this.boundingBox = new AABB(transform.position, transform.scale.x/2,transform.scale.y/2,transform.scale.z/2);
    }

    // In Entity
    public void update(float dt, ChunkManager chunkManager) {
        this.lastDt = dt;

        Vector3i playerChunk = new Vector3i(
                Math.floorDiv((int) transform.position.x, Chunk.CHUNK_SIZE),
                0,
                Math.floorDiv((int) transform.position.z, Chunk.CHUNK_SIZE)
        );
        if (chunkManager.getChunk(playerChunk) == null) {
            velocity.set(0, 0, 0);
            return;
        }

        applyGravity(dt);
        applyCollisions(chunkManager);

    }

    protected void move(Vector3f velocity, ChunkManager chunkManager) {
        transform.position.add(velocity);
        applyCollisions(chunkManager);
    }

    protected void applyCollisions(ChunkManager chunkManager) {
        if (chunkManager == null) return;
        onGround = false;


        // X
        transform.position.x += velocity.x * lastDt;
        boundingBox.setPosition(transform.position);
        for (Vector3i block : getOverlappingBlocks()) {
            if (!chunkManager.isBlockAt(block)) continue;
            AABB blockAABB = AABB.blockAABB(block.x, block.y, block.z);
            if (!boundingBox.intersects(blockAABB)) continue;
            if (velocity.x > 0) transform.position.x = blockAABB.getMin().x - boundingBox.getHalfExtents().x;
            if (velocity.x < 0) transform.position.x = blockAABB.getMax().x + boundingBox.getHalfExtents().x;
            velocity.x = 0;
            boundingBox.setPosition(transform.position);
        }

        // Y
        transform.position.y += velocity.y * lastDt;
        boundingBox.setPosition(transform.position);
        for (Vector3i block : getOverlappingBlocks()) {
            if (!chunkManager.isBlockAt(block)) continue;
            AABB blockAABB = AABB.blockAABB(block.x, block.y, block.z);
            if (!boundingBox.intersects(blockAABB)) continue;
            if (velocity.y > 0) transform.position.y = blockAABB.getMin().y - boundingBox.getHalfExtents().y;
            if (velocity.y < 0) {
                transform.position.y = blockAABB.getMax().y + boundingBox.getHalfExtents().y;
                onGround = true;
            }
            velocity.y = 0;
            boundingBox.setPosition(transform.position);
        }

        // Z
        transform.position.z += velocity.z * lastDt;
        boundingBox.setPosition(transform.position);
        for (Vector3i block : getOverlappingBlocks()) {
            if (!chunkManager.isBlockAt(block)) continue;
            AABB blockAABB = AABB.blockAABB(block.x, block.y, block.z);
            if (!boundingBox.intersects(blockAABB)) continue;
            if (velocity.z > 0) transform.position.z = blockAABB.getMin().z - boundingBox.getHalfExtents().z;
            if (velocity.z < 0) transform.position.z = blockAABB.getMax().z + boundingBox.getHalfExtents().z;
            velocity.z = 0;
            boundingBox.setPosition(transform.position);

        }

        if (!onGround) {
            int x = (int) Math.floor(transform.position.x + 0.5f);
            int y = (int) Math.floor(transform.position.y - boundingBox.getHalfExtents().y - 0.05f + 0.5f);
            int z = (int) Math.floor(transform.position.z + 0.5f);
            if (chunkManager.isBlockAt(new Vector3i(x, y, z)))
                onGround = true;
        }
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
        mesh.draw();
    }

    public Vector3f getPosition() {
        return new Vector3f(transform.position);
    }

    public void setPosition(Vector3f position){
        transform.position= position;
       velocity = new Vector3f(0,0,0);
       onGround = false;
    }

    public void cleanup(){
        mesh.cleanup();
    }
}
