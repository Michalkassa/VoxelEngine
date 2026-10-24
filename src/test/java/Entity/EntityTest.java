package Entity;

import Core.Transform;
import World.Chunk;
import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private static final float DT = 1f / 60f;
    private static final float HALF_HEIGHT = 0.85f;
    private static final float EPS = 1e-3f;

    private static class TestEntity extends Entity {
        TestEntity(Vector3f position) {
            super(new Transform(position, new Vector3f(0.75f, 1.7f, 0.75f)));
        }
    }

    private Set<Vector3i> solidBlocks;
    private boolean chunksLoaded;
    private ChunkManager chunkManager;

    @BeforeEach
    void setUp() {
        solidBlocks = new HashSet<>();
        chunksLoaded = true;
        Chunk loaded = new Chunk(new Vector3i(), null, new byte[1][1][1]);
        chunkManager = new ChunkManager(0) {
            @Override
            public Chunk getChunk(Vector3i position) {
                return chunksLoaded ? loaded : null;
            }

            @Override
            public boolean isBlockAt(Vector3i position) {
                return solidBlocks.contains(position);
            }
        };
    }

    private void addFloor(int y) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                solidBlocks.add(new Vector3i(x, y, z));
            }
        }
    }

    private void simulate(Entity entity, int steps) {
        for (int i = 0; i < steps; i++) {
            entity.update(DT, chunkManager);
        }
    }

    @Test
    void gravityAcceleratesFallingEntity() {
        TestEntity entity = new TestEntity(new Vector3f(0, 100, 0));

        entity.update(DT, chunkManager);

        assertEquals(-32f * DT, entity.getVelocity().y, EPS);
        assertTrue(entity.getPosition().y < 100);
    }

    @Test
    void fallSpeedKeepsIncreasingInAir() {
        TestEntity entity = new TestEntity(new Vector3f(0, 100, 0));

        simulate(entity, 30);

        assertEquals(-32f * DT * 30, entity.getVelocity().y, EPS);
    }

    @Test
    void entityLandsOnFloorAndStops() {
        addFloor(0);
        TestEntity entity = new TestEntity(new Vector3f(0, 5, 0));

        simulate(entity, 180);

        assertEquals(0.5f + HALF_HEIGHT, entity.getPosition().y, EPS);
        assertEquals(0f, entity.getVelocity().y, EPS);
        assertTrue(entity.onGround);
    }

    @Test
    void restingEntityStaysOnGround() {
        addFloor(0);
        TestEntity entity = new TestEntity(new Vector3f(0, 0.5f + HALF_HEIGHT, 0));

        simulate(entity, 120);

        assertEquals(0.5f + HALF_HEIGHT, entity.getPosition().y, EPS);
        assertTrue(entity.onGround);
    }

    @Test
    void fastFallDoesNotTunnelThroughFloor() {
        addFloor(0);
        TestEntity entity = new TestEntity(new Vector3f(0, 3, 0));
        entity.getVelocity().y = -50;

        simulate(entity, 60);

        assertEquals(0.5f + HALF_HEIGHT, entity.getPosition().y, EPS);
    }

    @Test
    void ceilingStopsUpwardMovement() {
        solidBlocks.add(new Vector3i(0, 5, 0));
        TestEntity entity = new TestEntity(new Vector3f(0, 2, 0));
        entity.getVelocity().y = 20;

        simulate(entity, 10);

        assertTrue(entity.getPosition().y + HALF_HEIGHT <= 4.5f + EPS, "head went into ceiling");
    }

    @Test
    void wallStopsHorizontalMovement() {
        addFloor(0);
        for (int y = 1; y <= 3; y++) solidBlocks.add(new Vector3i(2, y, 0));
        TestEntity entity = new TestEntity(new Vector3f(0, 0.5f + HALF_HEIGHT, 0));

        for (int i = 0; i < 60; i++) {
            entity.getVelocity().x = 5;
            entity.update(DT, chunkManager);
        }

        assertEquals(1.5f - 0.375f, entity.getPosition().x, EPS);
        assertEquals(0f, entity.getVelocity().x, EPS);
    }

    @Test
    void wallStopsMovementAlongZ() {
        addFloor(0);
        for (int y = 1; y <= 3; y++) solidBlocks.add(new Vector3i(0, y, -2));
        TestEntity entity = new TestEntity(new Vector3f(0, 0.5f + HALF_HEIGHT, 0));

        for (int i = 0; i < 60; i++) {
            entity.getVelocity().z = -5;
            entity.update(DT, chunkManager);
        }

        assertEquals(-1.5f + 0.375f, entity.getPosition().z, EPS);
        assertEquals(0f, entity.getVelocity().z, EPS);
    }

    @Test
    void entityInUnloadedChunkIsFrozen() {
        chunksLoaded = false;
        TestEntity entity = new TestEntity(new Vector3f(0, 100, 0));
        entity.getVelocity().set(3, 3, 3);

        simulate(entity, 10);

        assertEquals(new Vector3f(0, 100, 0), entity.getPosition());
        assertEquals(new Vector3f(0, 0, 0), entity.getVelocity());
    }

    @Test
    void setPositionResetsVelocityAndGroundState() {
        addFloor(0);
        TestEntity entity = new TestEntity(new Vector3f(0, 0.5f + HALF_HEIGHT, 0));
        simulate(entity, 5);
        entity.getVelocity().set(1, 2, 3);

        entity.setPosition(new Vector3f(10, 50, 10));

        assertEquals(new Vector3f(10, 50, 10), entity.getPosition());
        assertEquals(new Vector3f(0, 0, 0), entity.getVelocity());
        assertFalse(entity.onGround);
    }

    @Test
    void getPositionReturnsCopy() {
        TestEntity entity = new TestEntity(new Vector3f(1, 2, 3));

        entity.getPosition().set(9, 9, 9);

        assertEquals(new Vector3f(1, 2, 3), entity.getPosition());
    }

    @Test
    void getVelocityReturnsLiveVector() {
        TestEntity entity = new TestEntity(new Vector3f());

        entity.getVelocity().set(1, 2, 3);

        assertEquals(new Vector3f(1, 2, 3), entity.getVelocity());
    }
}
