package Entity;

import Core.Transform;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivingEntityTest {

    private static class TestLiving extends LivingEntity {
        boolean invulnerable = false;

        TestLiving(float maxHealth) {
            super(new Transform(new Vector3f(), new Vector3f(0.75f, 1.7f, 0.75f)), maxHealth);
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
}
