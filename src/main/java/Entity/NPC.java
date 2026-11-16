package Entity;

import Core.Transform;
import World.ChunkManager;
import org.joml.Vector3f;

import java.util.Random;

// A computer-controlled character that wanders around: it stands still for a while,
// then walks in a random direction, jumping up single-block steps in its way
public class NPC extends LivingEntity {

    public static final float MAX_HEALTH = 10f;
    public static final float WALK_SPEED = 2.5f;
    private static final float JUMP_FORCE = 9f;
    private static final Vector3f SIZE = new Vector3f(0.75f, 1.7f, 0.75f);

    private static final float MIN_IDLE_TIME = 1f;
    private static final float MAX_IDLE_TIME = 4f;
    private static final float MIN_WALK_TIME = 2f;
    private static final float MAX_WALK_TIME = 5f;
    // Walking slower than this fraction of full speed while on the ground means something is in the way
    private static final float BLOCKED_SPEED_FRACTION = 0.25f;
    // Time to get up to speed after setting off before checking if it is blocked
    private static final float BLOCKED_CHECK_DELAY = 0.3f;

    private final Random random;
    private boolean walking = false;
    private float stateTimeLeft;
    private float timeWalking = 0f;

    public NPC(Vector3f position) {
        this(position, new Random());
    }

    public NPC(Vector3f position, Random random) {
        super(new Transform(position, SIZE), MAX_HEALTH, WALK_SPEED, JUMP_FORCE);
        this.random = random;
        this.stateTimeLeft = randomBetween(MIN_IDLE_TIME, MAX_IDLE_TIME);
    }

    @Override
    public void update(float dt, ChunkManager chunkManager) {
        if (isDead()) {
            remove();
            return;
        }
        think(dt);
        super.update(dt, chunkManager);
    }

    // Decides where to walk this frame
    private void think(float dt) {
        stateTimeLeft -= dt;
        if (stateTimeLeft <= 0) {
            if (walking) {
                standStill(randomBetween(MIN_IDLE_TIME, MAX_IDLE_TIME));
            } else {
                float angle = random.nextFloat() * (float) (2 * Math.PI);
                walk(new Vector3f((float) Math.cos(angle), 0, (float) Math.sin(angle)),
                        randomBetween(MIN_WALK_TIME, MAX_WALK_TIME));
            }
        }

        if (walking) {
            timeWalking += dt;
            if (timeWalking > BLOCKED_CHECK_DELAY && isOnGround() && horizontalSpeed() < WALK_SPEED * BLOCKED_SPEED_FRACTION) {
                jump();
            }
        }
    }

    // Starts walking in the given direction for the given time
    void walk(Vector3f direction, float seconds) {
        walking = true;
        stateTimeLeft = seconds;
        timeWalking = 0f;
        setMoveDirection(direction);
    }

    // Stops and stands still for the given time
    void standStill(float seconds) {
        walking = false;
        stateTimeLeft = seconds;
        stopMoving();
    }

    public boolean isWalking() {
        return walking;
    }

    private float horizontalSpeed() {
        return (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    private float randomBetween(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
