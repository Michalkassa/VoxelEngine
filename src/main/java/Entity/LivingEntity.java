package Entity;

import Core.Transform;

// An entity with health, such as the player or an NPC
public abstract class LivingEntity extends Entity {

    private final float maxHealth;
    private float health;

    protected LivingEntity(Transform transform, float maxHealth) {
        super(transform);
        if (maxHealth <= 0) throw new IllegalArgumentException("maxHealth must be positive, was " + maxHealth);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    // Takes away health, never going below zero. Ignored while invulnerable
    public void damage(float amount) {
        if (amount <= 0 || isInvulnerable()) return;
        health = Math.max(0f, health - amount);
    }

    // Restores health, never going above the maximum
    public void heal(float amount) {
        if (amount <= 0) return;
        health = Math.min(maxHealth, health + amount);
    }

    protected boolean isInvulnerable() {
        return false;
    }
}
