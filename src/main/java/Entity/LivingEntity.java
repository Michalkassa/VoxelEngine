package Entity;

import Core.Transform;
import World.ChunkManager;
import org.joml.Vector3f;

// An entity with health that walks and jumps, such as the player or an NPC.
// Whatever controls it (keyboard input or AI) sets the direction it wants to walk in and asks it to jump
public abstract class LivingEntity extends Entity {

    private static final float ACCELERATION = 50f;
    private static final float GROUND_FRICTION = 30f;
    private static final float AIR_FRICTION = 5f;
    private static final float AIR_CONTROL = 8f;
    private static final float STOP_SPEED = 0.01f;

    private final float maxHealth;
    private float health;
    private final float walkSpeed;
    private final float jumpForce;

    // Horizontal direction the entity wants to walk in. Length 1 is full speed, shorter is slower
    private final Vector3f moveDirection = new Vector3f();

    protected LivingEntity(Transform transform, float maxHealth, float walkSpeed, float jumpForce) {
        super(transform);
        if (maxHealth <= 0) throw new IllegalArgumentException("maxHealth must be positive, was " + maxHealth);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.walkSpeed = walkSpeed;
        this.jumpForce = jumpForce;
    }

    @Override
    public void update(float dt, ChunkManager chunkManager) {
        applyMovement(dt);
        super.update(dt, chunkManager);
    }

    // Sets the direction to walk in. Height is ignored, and anything longer than 1 is shortened to 1
    public void setMoveDirection(Vector3f direction) {
        moveDirection.set(direction.x, 0, direction.z);
        if (moveDirection.lengthSquared() > 1) moveDirection.normalize();
    }

    public Vector3f getMoveDirection() {
        return new Vector3f(moveDirection);
    }

    public void stopMoving() {
        moveDirection.zero();
    }

    // Jumps if standing on the ground
    public void jump() {
        if (onGround) {
            velocity.y = jumpForce;
            onGround = false;
        }
    }

    public boolean isOnGround() {
        return onGround;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    // Speeds up towards the wanted direction, or slows down with friction when there is none
    protected void applyMovement(float dt) {
        if (moveDirection.lengthSquared() > 0) {
            float accel = hasFullControl() ? ACCELERATION : AIR_CONTROL;
            velocity.x += moveDirection.x * accel * dt;
            velocity.z += moveDirection.z * accel * dt;

            float maxSpeed = getMaxSpeed() * moveDirection.length();
            float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            if (horizontalSpeed > maxSpeed) {
                float scale = maxSpeed / horizontalSpeed;
                velocity.x *= scale;
                velocity.z *= scale;
            }
        } else {
            float friction = hasFullControl() ? GROUND_FRICTION : AIR_FRICTION;
            velocity.x -= velocity.x * friction * dt;
            velocity.z -= velocity.z * friction * dt;

            if (Math.abs(velocity.x) < STOP_SPEED) velocity.x = 0;
            if (Math.abs(velocity.z) < STOP_SPEED) velocity.z = 0;
        }
    }

    // Top horizontal speed when walking at full speed
    protected float getMaxSpeed() {
        return walkSpeed;
    }

    // Whether the entity has full grip to speed up and stop, rather than the weak control it has in the air
    protected boolean hasFullControl() {
        return onGround;
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
