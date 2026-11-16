package Entity;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

import Core.Camera;
import Core.Input;
import Core.Transform;
import World.Block;
import World.Chunk;
import World.ChunkManager;

public class Player extends LivingEntity {

    private static final float MOVE_SPEED = 6f;
    private static final float JUMP_FORCE = 9f;
    private static final float FLY_SPEED = 20f;
    private static final float FLY_VERTICAL_SPEED = 8f;
    private static final float DOUBLE_TAP_WINDOW = 0.3f;
    private static final float BREAK_INTERVAL = 0.25f;
    private static final float PLACE_INTERVAL = 0.25f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MAX_HEALTH = 20f;

    private Camera camera;

    private GameMode gameMode = GameMode.SURVIVAL;
    private boolean flying = false;
    private float timeSinceJumpPress = Float.MAX_VALUE;
    private float breakCooldown = 0f;
    private float placeCooldown = 0f;
    // Block placed with right click, until there is a hotbar to choose from
    private Block selectedBlock = Block.STONE;

    private float yaw = 0.0f;   // Rotation around Y axis
    private float pitch = 0.0f; // Rotation around X axis

    private Vector3f forward;
    private Vector3f right;

    public Player(Vector3f position, Vector3f scale) {
        super(new Transform(position, scale), MAX_HEALTH, MOVE_SPEED, JUMP_FORCE);
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

        placeCooldown -= deltaTime;
        if (Input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            placeTargetBlock(chunkManager);
        } else if (Input.isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT) && placeCooldown <= 0f) {
            placeTargetBlock(chunkManager);
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

        setMoveDirection(moveDirection);
    }

    private void breakTargetBlock(ChunkManager chunkManager) {
        breakCooldown = BREAK_INTERVAL;
        Vector3i blockpos = camera.shootRaycast();
        if (blockpos != null) {
            chunkManager.setBlockAt(blockpos, (byte) Block.AIR.ordinal());
        }
    }

    private void placeTargetBlock(ChunkManager chunkManager) {
        placeCooldown = PLACE_INTERVAL;
        Vector3i blockpos = camera.shootPlacementRaycast();
        if (blockpos != null) {
            placeBlock(chunkManager, blockpos);
        }
    }

    // Places the selected block unless the spot is taken, outside the world or would trap the player
    boolean placeBlock(ChunkManager chunkManager, Vector3i blockpos) {
        if (blockpos.y < 0 || blockpos.y >= Chunk.CHUNK_HEIGHT) return false;
        if (chunkManager.getChunk(new Vector3i(Math.floorDiv(blockpos.x, Chunk.CHUNK_SIZE), 0, Math.floorDiv(blockpos.z, Chunk.CHUNK_SIZE))) == null) return false;
        if (chunkManager.isBlockAt(blockpos)) return false;
        if (AABB.blockAABB(blockpos.x, blockpos.y, blockpos.z).intersects(boundingBox)) return false;

        chunkManager.setBlockAt(blockpos, (byte) selectedBlock.ordinal());
        return true;
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(Block block) {
        this.selectedBlock = block;
    }

    @Override
    protected float getMaxSpeed() {
        return flying ? FLY_SPEED : super.getMaxSpeed();
    }

    @Override
    protected boolean hasFullControl() {
        return flying || super.hasFullControl();
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

    @Override
    protected boolean isInvulnerable() {
        return gameMode == GameMode.CREATIVE;
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

    public Camera getCamera() {
        return camera;
    }

}