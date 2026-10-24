package Core;

import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RaycastTest {

    private Set<Vector3i> solidBlocks;
    private Raycast raycast;

    @BeforeEach
    void setUp() {
        solidBlocks = new HashSet<>();
        ChunkManager chunkManager = new ChunkManager(0) {
            @Override
            public boolean isBlockAt(Vector3i position) {
                return solidBlocks.contains(position);
            }
        };
        raycast = new Raycast(chunkManager, 4);
    }

    private Vector3i cast(Vector3f origin, Vector3f direction) {
        raycast.setOrigin(origin);
        raycast.setDirection(direction);
        return raycast.getBlockPosition();
    }

    @Test
    void returnsNullWhenNothingIsHit() {
        assertNull(cast(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));
    }

    @Test
    void hitsBlockStraightAhead() {
        solidBlocks.add(new Vector3i(3, 0, 0));

        assertEquals(new Vector3i(3, 0, 0), cast(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));
    }

    @Test
    void returnsNearestOfSeveralBlocks() {
        solidBlocks.add(new Vector3i(0, 0, -5));
        solidBlocks.add(new Vector3i(0, 0, -2));

        assertEquals(new Vector3i(0, 0, -2), cast(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1)));
    }

    @Test
    void ignoresBlocksBehindOrigin() {
        solidBlocks.add(new Vector3i(-2, 0, 0));

        assertNull(cast(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));
    }

    @Test
    void hitsBlockBelowWhenLookingDown() {
        solidBlocks.add(new Vector3i(4, 60, 4));

        assertEquals(new Vector3i(4, 60, 4), cast(new Vector3f(4, 62.5f, 4), new Vector3f(0, -1, 0)));
    }

    @Test
    void followsDiagonalDirection() {
        solidBlocks.add(new Vector3i(2, 2, 0));

        assertEquals(new Vector3i(2, 2, 0), cast(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0)));
    }

    @Test
    void directionLengthDoesNotMatter() {
        solidBlocks.add(new Vector3i(3, 0, 0));

        assertEquals(new Vector3i(3, 0, 0), cast(new Vector3f(0, 0, 0), new Vector3f(50, 0, 0)));
    }

    @Test
    void blocksBeyondMaxDistanceAreNotHit() {
        solidBlocks.add(new Vector3i(20, 0, 0));

        assertNull(cast(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));
    }

    @Test
    void doesNotModifyOriginOrDirection() {
        Vector3f origin = new Vector3f(1, 2, 3);
        Vector3f direction = new Vector3f(0, 0, 5);

        cast(origin, direction);

        assertEquals(new Vector3f(1, 2, 3), origin);
        assertEquals(new Vector3f(0, 0, 5), direction);
    }

    @Test
    void cameraShootRaycastUsesItsPositionAndDirection() {
        solidBlocks.add(new Vector3i(13, 64, 10));
        Camera camera = new Camera(new Vector3f(10, 64, 10), new Vector3f());
        camera.setRaycast(raycast);
        camera.update(0.016f);

        assertEquals(new Vector3i(13, 64, 10), camera.shootRaycast());
    }
}
