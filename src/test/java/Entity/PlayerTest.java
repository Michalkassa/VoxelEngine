package Entity;

import World.Chunk;
import World.ChunkManager;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private static final float DT = 1f / 60f;
    private static final float EPS = 1e-3f;
    private static final float STANDING_Y = 0.5f + 0.85f;

    private Set<Vector3i> solidBlocks;
    private ChunkManager chunkManager;
    private Player player;

    @BeforeEach
    void setUp() {
        solidBlocks = new HashSet<>();
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
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
        player = new Player(new Vector3f(0, STANDING_Y, 0), new Vector3f(0.75f, 1.7f, 0.75f));
    }

    private void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            player.update(DT, chunkManager);
        }
    }

    @Test
    void startsInSurvivalWithFullHealthAndNotFlying() {
        assertEquals(GameMode.SURVIVAL, player.getGameMode());
        assertEquals(Player.MAX_HEALTH, player.getHealth());
        assertEquals(20f, Player.MAX_HEALTH);
        assertFalse(player.isFlying());
    }

    @Test
    void toggleGameModeSwitchesBackAndForth() {
        player.toggleGameMode();
        assertEquals(GameMode.CREATIVE, player.getGameMode());

        player.toggleGameMode();
        assertEquals(GameMode.SURVIVAL, player.getGameMode());
    }

    @Test
    void setGameModeSetsMode() {
        player.setGameMode(GameMode.CREATIVE);

        assertEquals(GameMode.CREATIVE, player.getGameMode());
    }

    @Test
    void damageReducesHealthInSurvival() {
        player.damage(5);

        assertEquals(15f, player.getHealth());
    }

    @Test
    void healthDoesNotDropBelowZero() {
        player.damage(50);

        assertEquals(0f, player.getHealth());
    }

    @Test
    void creativePlayersTakeNoDamage() {
        player.setGameMode(GameMode.CREATIVE);

        player.damage(10);

        assertEquals(Player.MAX_HEALTH, player.getHealth());
    }

    @Test
    void healRestoresHealthUpToMaximum() {
        player.damage(10);

        player.heal(4);
        assertEquals(14f, player.getHealth());

        player.heal(100);
        assertEquals(Player.MAX_HEALTH, player.getHealth());
    }

    @Test
    void healthCarriesOverWhenSwitchingModes() {
        player.damage(7);

        player.toggleGameMode();
        player.toggleGameMode();

        assertEquals(13f, player.getHealth());
    }

    @Test
    void playerRestsOnGroundWithoutInput() {
        simulate(60);

        assertEquals(STANDING_Y, player.getPosition().y, EPS);
        assertEquals(new Vector3f(0, 0, 0), player.getVelocity());
    }

    @Test
    void jumpLaunchesPlayerWhenOnGround() {
        simulate(2);

        player.jump();

        assertEquals(9f, player.getVelocity().y, EPS);
    }

    @Test
    void jumpReachesAboutOneAndAQuarterBlocks() {
        simulate(2);
        player.jump();

        float peak = 0;
        for (int i = 0; i < 60; i++) {
            player.update(DT, chunkManager);
            peak = Math.max(peak, player.getPosition().y - STANDING_Y);
        }

        assertTrue(peak > 1.0f && peak < 1.4f, "jump peak " + peak);
    }

    @Test
    void cannotJumpInMidAir() {
        player.setPosition(new Vector3f(0, 50, 0));
        player.update(DT, chunkManager);
        float before = player.getVelocity().y;

        player.jump();

        assertEquals(before, player.getVelocity().y, EPS);
    }

    @Test
    void groundFrictionStopsSlidingPlayer() {
        simulate(2);
        player.getVelocity().x = 6;

        simulate(60);

        assertEquals(0f, player.getVelocity().x, EPS);
    }

    @Test
    void cameraFollowsPlayerAtEyeHeight() {
        simulate(1);

        Vector3f eye = player.getCamera().position;
        assertEquals(player.getPosition().x, eye.x, EPS);
        assertEquals(player.getPosition().y + 0.85f, eye.y, EPS);
        assertEquals(player.getPosition().z, eye.z, EPS);
    }

    @Test
    void defaultFacingIsNegativeZ() {
        assertEquals(0f, player.getForward().x, EPS);
        assertEquals(-1f, player.getForward().z, EPS);
        assertEquals(1f, player.getRight().x, EPS);
    }

    @Test
    void setRotationTurnsForwardAndRightVectors() {
        player.setRotation(90, 10);

        assertEquals(1f, player.getForward().x, EPS);
        assertEquals(0f, player.getForward().z, EPS);
        assertEquals(0f, player.getForward().y, EPS);
        assertEquals(1f, player.getRight().z, EPS);
        assertEquals(new Vector3f(10, 90, 0), player.getRotation());
    }

    @Test
    void directionGettersReturnCopies() {
        player.getForward().set(5, 5, 5);
        player.getRight().set(5, 5, 5);

        assertEquals(-1f, player.getForward().z, EPS);
        assertEquals(1f, player.getRight().x, EPS);
    }

    @Test
    void cameraPointsWherePlayerFaces() {
        player.setRotation(90, 0);
        simulate(1);
        player.getCamera().update(DT);

        assertEquals(1f, player.getCamera().direction.x, EPS);
        assertEquals(0f, player.getCamera().direction.z, EPS);
    }
}
