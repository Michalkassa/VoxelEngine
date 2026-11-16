package Entity;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

class NPCSpawnerTest {

    private static final float EPS = 1e-3f;
    private static final Vector3f CENTER = new Vector3f(100, 70, -50);
    // Rounding a spot to the nearest block centre moves it at most this far
    private static final float ROUNDING = (float) Math.sqrt(0.5);

    // Hilly ground so each column has a different height
    private static final ToIntFunction<Vector3i> HILLS = pos -> 60 + Math.floorMod(pos.x * 3 + pos.z * 7, 11);

    private List<NPC> spawn(long seed) {
        return NPCSpawner.spawnAround(CENTER, 20, 4, 10, new Random(seed), HILLS);
    }

    @Test
    void spawnsTheRequestedNumberOfNpcs() {
        assertEquals(20, spawn(1).size());
        assertEquals(0, NPCSpawner.spawnAround(CENTER, 0, 4, 10, new Random(1), HILLS).size());
    }

    @Test
    void npcsSpawnWithinTheDistanceRing() {
        for (NPC npc : spawn(2)) {
            Vector3f p = npc.getPosition();
            float distance = (float) Math.hypot(p.x - CENTER.x, p.z - CENTER.z);
            assertTrue(distance >= 4 - ROUNDING && distance <= 10 + ROUNDING, "distance " + distance);
        }
    }

    @Test
    void npcsSpawnOnBlockCentres() {
        for (NPC npc : spawn(3)) {
            Vector3f p = npc.getPosition();
            assertEquals(Math.round(p.x), p.x, EPS);
            assertEquals(Math.round(p.z), p.z, EPS);
        }
    }

    @Test
    void npcsStandJustAboveTheGroundOfTheirColumn() {
        for (NPC npc : spawn(4)) {
            Vector3f p = npc.getPosition();
            int groundY = HILLS.applyAsInt(new Vector3i(Math.round(p.x), 0, Math.round(p.z)));
            float feet = p.y - NPC.HEIGHT / 2;
            float groundTop = groundY + 0.5f;
            assertTrue(feet >= groundTop && feet < groundTop + 0.1f, "feet " + feet + ", ground top " + groundTop);
        }
    }

    @Test
    void sameSeedSpawnsInTheSamePlaces() {
        List<NPC> a = spawn(5);
        List<NPC> b = spawn(5);

        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getPosition(), b.get(i).getPosition());
        }
    }

    @Test
    void npcsAreSeparateEntities() {
        List<NPC> npcs = spawn(6);

        assertEquals(npcs.size(), npcs.stream().map(Entity::getId).distinct().count());
    }
}
