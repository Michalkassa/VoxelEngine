package Entity;

import World.Chunk;
import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NPCTest {

    private static final float DT = 1f / 60f;
    private static final float EPS = 1e-3f;
    private static final float STANDING_Y = 0.5f + 0.85f;
    private static final int ONE_SECOND = 60;

    private Set<Vector3i> solidBlocks;
    private ChunkManager chunkManager;

    @BeforeEach
    void setUp() {
        solidBlocks = new HashSet<>();
        for (int x = -60; x <= 60; x++) {
            for (int z = -60; z <= 60; z++) {
                solidBlocks.add(new Vector3i(x, 0, z));
            }
        }
        Chunk loaded = new Chunk(new Vector3i(), null, new byte[1][1][1]);
        chunkManager = new ChunkManager(0) {
            @Override
            public Chunk getChunk(Vector3i position) {
                return loaded;
            }

            @Override
            public boolean isBlockAt(Vector3i position) {
                return solidBlocks.contains(position);
            }
        };
    }

    private NPC standingNpc(long seed) {
        NPC npc = new NPC(new Vector3f(0, STANDING_Y, 0), new Random(seed));
        npc.update(DT, chunkManager);
        return npc;
    }

    private void simulate(NPC npc, int steps) {
        for (int i = 0; i < steps; i++) {
            npc.update(DT, chunkManager);
        }
    }

    private float horizontalDistanceFromStart(NPC npc) {
        Vector3f p = npc.getPosition();
        return (float) Math.sqrt(p.x * p.x + p.z * p.z);
    }

    @Test
    void startsStandingStillWithFullHealth() {
        NPC npc = new NPC(new Vector3f(0, STANDING_Y, 0), new Random(1));

        assertFalse(npc.isWalking());
        assertEquals(new Vector3f(), npc.getMoveDirection());
        assertEquals(NPC.MAX_HEALTH, npc.getHealth());
    }

    @Test
    void staysPutForAtLeastTheShortestIdleTime() {
        NPC npc = standingNpc(1);

        simulate(npc, ONE_SECOND - 5);

        assertFalse(npc.isWalking());
        assertEquals(0f, horizontalDistanceFromStart(npc), EPS);
    }

    @Test
    void wandersAwayFromWhereItStarted() {
        NPC npc = standingNpc(1);

        simulate(npc, 15 * ONE_SECOND);

        assertTrue(horizontalDistanceFromStart(npc) > 1f, "NPC did not wander");
    }

    @Test
    void takesTurnsWalkingAndStandingStill() {
        NPC npc = standingNpc(2);
        int walkingStarts = 0;
        int stops = 0;
        boolean wasWalking = npc.isWalking();

        for (int i = 0; i < 60 * ONE_SECOND; i++) {
            npc.update(DT, chunkManager);
            if (npc.isWalking() && !wasWalking) walkingStarts++;
            if (!npc.isWalking() && wasWalking) stops++;
            wasWalking = npc.isWalking();
        }

        assertTrue(walkingStarts >= 3, "walked only " + walkingStarts + " times");
        assertTrue(stops >= 3, "stopped only " + stops + " times");
    }

    @Test
    void neverGoesFasterThanWalkingSpeed() {
        NPC npc = standingNpc(3);

        for (int i = 0; i < 30 * ONE_SECOND; i++) {
            npc.update(DT, chunkManager);
            Vector3f v = npc.getVelocity();
            float speed = (float) Math.sqrt(v.x * v.x + v.z * v.z);
            assertTrue(speed <= NPC.WALK_SPEED + EPS, "speed " + speed);
        }
    }

    @Test
    void staysOnFlatGroundWhileWandering() {
        NPC npc = standingNpc(4);

        simulate(npc, 30 * ONE_SECOND);

        assertEquals(STANDING_Y, npc.getPosition().y, EPS);
    }

    @Test
    void sameSeedWandersTheSameWay() {
        NPC a = standingNpc(42);
        NPC b = standingNpc(42);

        simulate(a, 20 * ONE_SECOND);
        simulate(b, 20 * ONE_SECOND);

        assertEquals(a.getPosition(), b.getPosition());
    }

    @Test
    void jumpsUpASingleBlockStep() {
        for (int x = 3; x <= 60; x++)
            for (int z = -60; z <= 60; z++) solidBlocks.add(new Vector3i(x, 1, z));
        NPC npc = standingNpc(1);

        npc.walk(new Vector3f(1, 0, 0), 10);
        simulate(npc, 4 * ONE_SECOND);

        assertTrue(npc.getPosition().x > 3, "NPC did not climb the step, x = " + npc.getPosition().x);
        assertEquals(STANDING_Y + 1, npc.getPosition().y, EPS);
    }

    @Test
    void cannotGetOverATwoBlockWall() {
        for (int z = -60; z <= 60; z++) {
            solidBlocks.add(new Vector3i(3, 1, z));
            solidBlocks.add(new Vector3i(3, 2, z));
        }
        NPC npc = standingNpc(1);

        npc.walk(new Vector3f(1, 0, 0), 10);
        simulate(npc, 4 * ONE_SECOND);

        assertTrue(npc.getPosition().x < 2.5f, "NPC got through the wall");
    }

    @Test
    void standStillStopsWalking() {
        NPC npc = standingNpc(1);
        npc.walk(new Vector3f(1, 0, 0), 10);
        simulate(npc, ONE_SECOND);

        npc.standStill(10);
        simulate(npc, ONE_SECOND);

        assertFalse(npc.isWalking());
        assertEquals(0f, npc.getVelocity().x, EPS);
    }

    @Test
    void deadNpcIsRemovedFromTheWorld() {
        EntityManager manager = new EntityManager();
        NPC npc = manager.add(standingNpc(1));

        npc.damage(NPC.MAX_HEALTH);
        manager.update(DT, chunkManager);
        manager.update(DT, chunkManager);

        assertTrue(npc.isDead());
        assertTrue(npc.isRemoved());
        assertNull(manager.get(npc.getId()));
    }
}
