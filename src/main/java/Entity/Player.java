package Entity;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

import Core.Camera;
import Core.Input;
import Core.Transform;
import World.Block;
import World.ChunkManager;

public class Player extends Entity {

    private static final float MOVE_SPEED = 6f;
    private static final float JUMP_FORCE = 9f;
    private static final float FLY_SPEED = 20f;
    private static final float FLY_VERTICAL_SPEED = 8f;
    private static final float DOUBLE_TAP_WINDOW = 0.3f;
    private static final float BREAK_INTERVAL = 0.25f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float ACCELERATION = 50f;
    private static final float GROUND_FRICTION = 30f;
    private static final float AIR_FRICTION = 5f;
    private static final float AIR_CONTROL = 8f;
    public static final float MAX_HEALTH = 20f;

    private Camera camera;

    private GameMode gameMode = GameMode.SURVIVAL;
    private float health = MAX_HEALTH;
    private boolean flying = false;
    private float timeSinceJumpPress = Float.MAX_VALUE;
    private float breakCooldown = 0f;

    private float yaw = 0.0f;   // Rotation around Y axis
    private float pitch = 0.0f; // Rotation around X axis

    private Vector3f forward;
    private Vector3f right;

    private final float WIDTH = 0.75f;
    private final float HEIGHT = 1.7f;
    private final float LENGTH = 0.75f;

    public Player(Vector3f position, Vector3f scale) {
        super(new Transform(position,scale));
        this.forward = new Vector3f(0, 0, -1);
        this.right = new Vector3f(1, 0, 0);
        this.camera = new Camera(position, new Vector3f(0, 0, 0));
        updateDirectionVectors();
        updateCameraPosition();
    }

    @Override
    public void update(float dt, ChunkManager chunkManager) {
        handleInput(dt , chunkManager);
        super.update(dt, chunkManager);
        updateCameraPosition();
    }

    @Override
    public void render() {
       return;
    }

    public void handleInput(float deltaTime, ChunkManager chunkManager) {
        // Mouse look
        float deltaX = Input.getMouseDeltaX() * MOUSE_SENSITIVITY;
        float deltaY = Input.getMouseDeltaY() * MOUSE_SENSITIVITY;

        yaw += deltaX;
        pitch -= deltaY;

        // Clamp pitch to prevent camera flipping
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateDirectionVectors();

        if (Input.isKeyPressed(GLFW.GLFW_KEY_G)) {
            toggleGameMode();
        }

        Vector3f moveDirection = new Vector3f();

        if (Input.isKeyDown(GLFW.GLFW_KEY_W)) {
            moveDirection.add(forward);
        }
        if (Input.isKeyDown(GLFW.GLFW_KEY_S)) {
            moveDirection.sub(forward);
        }
        if (Input.isKeyDown(GLFW.GLFW_KEY_A)) {
            moveDirection.sub(right);
        }
        if (Input.isKeyDown(GLFW.GLFW_KEY_D)) {
            moveDirection.add(right);
        }
        breakCooldown -= deltaTime;
        if (Input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            breakTargetBlock(chunkManager);
        } else if (Input.isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && breakCooldown <= 0f) {
            breakTargetBlock(chunkManager);
        }



        if (flying && onGround) {
            flying = false;
        }

        timeSinceJumpPress += deltaTime;
        if (Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            if (gameMode == GameMode.CREATIVE && timeSinceJumpPress < DOUBLE_TAP_WINDOW) {
                flying = !flying;
                velocity.y = 0;
                timeSinceJumpPress = Float.MAX_VALUE;
            } else {
                timeSinceJumpPress = 0;
                if (!flying) jump();
            }
        }

        if (flying) {
            velocity.y = 0;
            if (Input.isKeyDown(GLFW.GLFW_KEY_SPACE)) velocity.y += FLY_VERTICAL_SPEED;
            if (Input.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) velocity.y -= FLY_VERTICAL_SPEED;
        }

        float maxSpeed = flying ? FLY_SPEED : MOVE_SPEED;

        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalize();

            float accel = onGround || flying ? ACCELERATION : AIR_CONTROL;

            velocity.x += moveDirection.x * accel * deltaTime;
            velocity.z += moveDirection.z * accel * deltaTime;

            float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            if (horizontalSpeed > maxSpeed) {
                float scale = maxSpeed / horizontalSpeed;
                velocity.x *= scale;
                velocity.z *= scale;
            }
        }
        else {
            float friction = onGround || flying ? GROUND_FRICTION : AIR_FRICTION;
            velocity.x -= velocity.x * friction * deltaTime;
            velocity.z -= velocity.z * friction * deltaTime;

            if (Math.abs(velocity.x) < 0.01f) velocity.x = 0;
            if (Math.abs(velocity.z) < 0.01f) velocity.z = 0;
        }
    }

    private void breakTargetBlock(ChunkManager chunkManager) {
        breakCooldown = BREAK_INTERVAL;
        Vector3i blockpos = camera.shootRaycast();
        if (blockpos != null) {
            chunkManager.setBlockAt(blockpos, (byte) Block.AIR.ordinal());
        }
    }

    public void jump() {
        if (onGround) {
            System.out.println("Jump");
            velocity.y = JUMP_FORCE;
            onGround = false;
        }
    }

    @Override
    protected void applyGravity(float dt) {
        if (flying) return;
        super.applyGravity(dt);
    }

    public void toggleGameMode() {
        gameMode = gameMode == GameMode.SURVIVAL ? GameMode.CREATIVE : GameMode.SURVIVAL;
        if (gameMode == GameMode.SURVIVAL) flying = false;
    }

    public boolean isFlying() {
        return flying;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        if (gameMode == GameMode.SURVIVAL) flying = false;
    }

    public float getHealth() {
        return health;
    }

    public void damage(float amount) {
        if (gameMode == GameMode.CREATIVE) return;
        health = Math.max(0f, health - amount);
    }

    public void heal(float amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    private void updateCameraPosition() {
        // Set camera position at player's eye height
        Vector3f cameraPos = new Vector3f(
                transform.position.x,
                transform.position.y + transform.scale.y/2,
                transform.position.z
        );
        camera.setPosition(cameraPos);

        // Update camera rotation based on player's yaw and pitch
        camera.setYaw((float) Math.toRadians(yaw) - (float)Math.PI / 2f);
        camera.setPitch((float) Math.toRadians(pitch));
    }

    public Vector3f getRotation() {
        return new Vector3f(pitch, yaw, 0);
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        updateDirectionVectors();
    }

    public Vector3f getForward() {
        return new Vector3f(forward);
    }

    public Vector3f getRight() {
        return new Vector3f(right);
    }

    private void updateDirectionVectors() {
        // Calculate forward vector (ignore pitch for movement, only for looking)
        float yawRad = (float) Math.toRadians(yaw);
        forward.x = (float) Math.sin(yawRad);
        forward.y = 0;
        forward.z = (float) -Math.cos(yawRad);
        forward.normalize();

        // Calculate right vector
        right.x = (float) Math.cos(yawRad);
        right.y = 0;
        right.z = (float) Math.sin(yawRad);
        right.normalize();
    }


    public Vector3f getPosition() {
        return new Vector3f(transform.position.x, transform.position.y, transform.position.z);
    }

    public Camera getCamera() {
        return camera;
    }

}