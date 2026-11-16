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

class LivingEntityTest {

    private static final float DT = 1f / 60f;
    private static final float EPS = 1e-3f;
    private static final float WALK_SPEED = 4f;
    private static final float JUMP_FORCE = 9f;
    private static final float STANDING_Y = 0.5f + 0.85f;

    private static class TestLiving extends LivingEntity {
        boolean invulnerable = false;

        TestLiving(float maxHealth) {
            this(maxHealth, new Vector3f());
        }

        TestLiving(float maxHealth, Vector3f position) {
            super(new Transform(position, new Vector3f(0.75f, 1.7f, 0.75f)), maxHealth, WALK_SPEED, JUMP_FORCE);
        }

        @Override
        protected boolean isInvulnerable() {
            return invulnerable;
        }
    }

    @Test
    void startsWithFullHealth() {
        TestLiving entity = new TestLiving(10);

        assertEquals(10f, entity.getHealth());
        assertEquals(10f, entity.getMaxHealth());
        assertFalse(entity.isDead());
    }

    @Test
    void maxHealthMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new TestLiving(0));
        assertThrows(IllegalArgumentException.class, () -> new TestLiving(-5));
    }

    @Test
    void damageReducesHealth() {
        TestLiving entity = new TestLiving(10);

        entity.damage(3);

        assertEquals(7f, entity.getHealth());
    }

    @Test
    void healthStopsAtZeroAndEntityDies() {
        TestLiving entity = new TestLiving(10);

        entity.damage(25);

        assertEquals(0f, entity.getHealth());
        assertTrue(entity.isDead());
    }

    @Test
    void healStopsAtMaximum() {
        TestLiving entity = new TestLiving(10);
        entity.damage(6);

        entity.heal(2);
        assertEquals(6f, entity.getHealth());

        entity.heal(100);
        assertEquals(10f, entity.getHealth());
    }

    @Test
    void negativeAmountsAreIgnored() {
        TestLiving entity = new TestLiving(10);
        entity.damage(4);

        entity.damage(-3);
        assertEquals(6f, entity.getHealth(), "negative damage healed the entity");

        entity.heal(-3);
        assertEquals(6f, entity.getHealth(), "negative heal damaged the entity");
    }

    @Test
    void invulnerableEntitiesTakeNoDamageButCanHeal() {
        TestLiving entity = new TestLiving(10);
        entity.damage(5);
        entity.invulnerable = true;

        entity.damage(5);
        entity.heal(1);

        assertEquals(6f, entity.getHealth());
    }

    // Movement

    private Set<Vector3i> solidBlocks;
    private ChunkManager chunkManager;

    @BeforeEach
    void setUp() {
        solidBlocks = new HashSet<>();
        for (int x = -20; x <= 20; x++) {
            for (int z = -20; z <= 20; z++) {
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

    private TestLiving standingEntity() {
        TestLiving entity = new TestLiving(10, new Vector3f(0, STANDING_Y, 0));
        simulate(entity, 2);
        return entity;
    }

    private void simulate(TestLiving entity, int steps) {
        for (int i = 0; i < steps; i++) {
            entity.update(DT, chunkManager);
        }
    }

    private float horizontalSpeed(TestLiving entity) {
        Vector3f v = entity.getVelocity();
        return (float) Math.sqrt(v.x * v.x + v.z * v.z);
    }

    @Test
    void walkingSpeedsUpToWalkSpeedAndNoFaster() {
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(1, 0, 0));

        entity.update(DT, chunkManager);
        assertTrue(horizontalSpeed(entity) > 0 && horizontalSpeed(entity) < WALK_SPEED, "should speed up gradually");

        simulate(entity, 60);
        assertEquals(WALK_SPEED, entity.getVelocity().x, EPS);
        assertEquals(0f, entity.getVelocity().z, EPS);
        assertTrue(entity.getPosition().x > 2, "entity did not walk forward");
    }

    @Test
    void diagonalWalkingIsNotFaster() {
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(1, 0, 1));

        simulate(entity, 60);

        assertEquals(WALK_SPEED, horizontalSpeed(entity), EPS);
    }

    @Test
    void shortDirectionWalksSlower() {
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(0.5f, 0, 0));

        simulate(entity, 60);

        assertEquals(WALK_SPEED / 2, horizontalSpeed(entity), EPS);
    }

    @Test
    void moveDirectionIgnoresHeightAndIsCappedAtLengthOne() {
        TestLiving entity = new TestLiving(10);

        entity.setMoveDirection(new Vector3f(3, 7, 4));

        assertEquals(new Vector3f(0.6f, 0, 0.8f), entity.getMoveDirection());
    }

    @Test
    void stoppingLetsFrictionBringEntityToRest() {
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(1, 0, 0));
        simulate(entity, 60);

        entity.stopMoving();
        simulate(entity, 30);

        assertEquals(new Vector3f(), entity.getMoveDirection());
        assertEquals(0f, horizontalSpeed(entity), EPS);
    }

    @Test
    void controlInTheAirIsWeakerThanOnTheGround() {
        TestLiving grounded = standingEntity();
        TestLiving airborne = new TestLiving(10, new Vector3f(0, 100, 0));
        airborne.update(DT, chunkManager);

        grounded.setMoveDirection(new Vector3f(1, 0, 0));
        airborne.setMoveDirection(new Vector3f(1, 0, 0));
        grounded.update(DT, chunkManager);
        airborne.update(DT, chunkManager);

        assertTrue(airborne.getVelocity().x < grounded.getVelocity().x);
        assertTrue(airborne.getVelocity().x > 0);
    }

    @Test
    void walkingIntoAWallStopsAtTheWall() {
        for (int y = 1; y <= 3; y++) solidBlocks.add(new Vector3i(3, y, 0));
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(1, 0, 0));

        simulate(entity, 120);

        assertEquals(2.5f - 0.375f, entity.getPosition().x, EPS);
    }

    @Test
    void jumpWorksOnlyFromTheGround() {
        TestLiving entity = standingEntity();
        assertTrue(entity.isOnGround());

        entity.jump();
        assertEquals(JUMP_FORCE, entity.getVelocity().y, EPS);
        assertFalse(entity.isOnGround());

        entity.update(DT, chunkManager);
        float before = entity.getVelocity().y;
        entity.jump();
        assertEquals(before, entity.getVelocity().y, EPS, "jumped again in mid air");
    }

    @Test
    void jumpingEntityLandsBackOnTheGround() {
        TestLiving entity = standingEntity();

        entity.jump();
        simulate(entity, 120);

        assertEquals(STANDING_Y, entity.getPosition().y, EPS);
        assertTrue(entity.isOnGround());
    }

    @Test
    void canJumpOntoAOneBlockStepWhileWalking() {
        for (int x = 3; x <= 20; x++)
            for (int z = -20; z <= 20; z++) solidBlocks.add(new Vector3i(x, 1, z));
        TestLiving entity = standingEntity();
        entity.setMoveDirection(new Vector3f(1, 0, 0));

        for (int i = 0; i < 90; i++) {
            entity.jump();
            entity.update(DT, chunkManager);
        }
        entity.stopMoving();
        simulate(entity, 60);

        assertTrue(entity.getPosition().x > 3, "did not get onto the step");
        assertEquals(STANDING_Y + 1, entity.getPosition().y, EPS);
    }
}
