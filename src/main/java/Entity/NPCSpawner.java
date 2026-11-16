package Entity;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

// Places NPCs standing on the ground at random spots in a ring around a point
public final class NPCSpawner {

    // Gap left under a new NPC's feet so it never starts inside the ground
    private static final float SPAWN_GAP = 0.05f;

    private NPCSpawner() {
    }

    // surfaceHeight gives the y of the highest block in a column, looked up by its x and z
    public static List<NPC> spawnAround(Vector3f center, int count, float minDistance, float maxDistance,
                                        Random random, ToIntFunction<Vector3i> surfaceHeight) {
        List<NPC> npcs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float distance = minDistance + random.nextFloat() * (maxDistance - minDistance);
            int x = Math.round(center.x + (float) Math.cos(angle) * distance);
            int z = Math.round(center.z + (float) Math.sin(angle) * distance);

            int groundY = surfaceHeight.applyAsInt(new Vector3i(x, 0, z));
            float y = groundY + 0.5f + NPC.HEIGHT / 2 + SPAWN_GAP;
            npcs.add(new NPC(new Vector3f(x, y, z), new Random(random.nextLong())));
        }
        return npcs;
    }
}
