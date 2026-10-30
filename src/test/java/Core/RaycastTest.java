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

    private Vector3i castPlacement(Vector3f origin, Vector3f direction) {
        raycast.setOrigin(origin);
        raycast.setDirection(direction);
        return raycast.getPlacementPosition();
    }

    @Test
    void placementIsNullWhenNothingIsHit() {
        assertNull(castPlacement(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));
    }

    @Test
    void placementGoesOnTheFaceFacingTheRay() {
        solidBlocks.add(new Vector3i(3, 0, 0));
        assertEquals(new Vector3i(2, 0, 0), castPlacement(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0)));

        solidBlocks.add(new Vector3i(-3, 0, 0));
        assertEquals(new Vector3i(-2, 0, 0), castPlacement(new Vector3f(0, 0, 0), new Vector3f(-1, 0, 0)));

        solidBlocks.add(new Vector3i(0, 0, 4));
        assertEquals(new Vector3i(0, 0, 3), castPlacement(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1)));
    }

    @Test
    void placementLookingDownGoesOnTop() {
        solidBlocks.add(new Vector3i(4, 60, 4));

        assertEquals(new Vector3i(4, 61, 4), castPlacement(new Vector3f(4, 62.5f, 4), new Vector3f(0, -1, 0)));
    }

    @Test
    void placementLookingUpGoesUnderneath() {
        solidBlocks.add(new Vector3i(0, 5, 0));

        assertEquals(new Vector3i(0, 4, 0), castPlacement(new Vector3f(0, 2, 0), new Vector3f(0, 1, 0)));
    }

    @Test
    void steepRayOntoGroundPlacesOnTopNotBeside() {
        // Looking down at the floor at an angle, the ray comes through the top face
        for (int x = -5; x <= 5; x++) solidBlocks.add(new Vector3i(x, 0, 0));

        Vector3i placement = castPlacement(new Vector3f(0, 2.1f, 0), new Vector3f(1, -1.2f, 0));
        Vector3i hit = cast(new Vector3f(0, 2.1f, 0), new Vector3f(1, -1.2f, 0));
        assertEquals(new Vector3i(hit.x, 1, 0), placement);
    }

    @Test
    void placementIsAlwaysEmptyAndBesideTheHitBlock() {
        java.util.Random random = new java.util.Random(3);
        for (int i = 0; i < 300; i++) {
            solidBlocks.add(new Vector3i(random.nextInt(9) - 4, random.nextInt(9) - 4, random.nextInt(9) - 4));
        }
        solidBlocks.remove(new Vector3i(0, 0, 0));

        for (int i = 0; i < 500; i++) {
            Vector3f direction = new Vector3f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f);
            if (direction.lengthSquared() < 1e-4f) continue;
            Vector3i hit = cast(new Vector3f(), direction);
            Vector3i placement = castPlacement(new Vector3f(), direction);
            if (hit == null) {
                assertNull(placement);
                continue;
            }
            assertEquals(1, Math.abs(placement.x - hit.x) + Math.abs(placement.y - hit.y) + Math.abs(placement.z - hit.z),
                    "placement " + placement + " does not share a face with " + hit);
            assertFalse(solidBlocks.contains(placement), "placement " + placement + " is already solid");
        }
    }

    @Test
    void cameraShootPlacementRaycastUsesItsPositionAndDirection() {
        solidBlocks.add(new Vector3i(13, 64, 10));
        Camera camera = new Camera(new Vector3f(10, 64, 10), new Vector3f());
        camera.setRaycast(raycast);
        camera.update(0.016f);

        assertEquals(new Vector3i(12, 64, 10), camera.shootPlacementRaycast());
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
