package Entity;

import Core.Camera;
import Core.Input;
import Core.Transform;
import World.Chunk;
import World.ChunkManager;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Player extends Entity {

    private static final float MOVE_SPEED = 6.0f;
    private static final float JUMP_FORCE = 24f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float ACCELERATION = 50f;
    private static final float GROUND_FRICTION = 30f;
    private static final float AIR_FRICTION = 5f;
    private static final float AIR_CONTROL = 8f;



    private Camera camera;

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
        handleInput(dt);
        super.update(dt, chunkManager);
        updateCameraPosition();
    }

    @Override
    public void render() {
       return;
    }

    public void handleInput(float deltaTime) {
        // Mouse look
        float deltaX = Input.getMouseDeltaX() * MOUSE_SENSITIVITY;
        float deltaY = Input.getMouseDeltaY() * MOUSE_SENSITIVITY;

        yaw += deltaX;
        pitch -= deltaY;

        // Clamp pitch to prevent camera flipping
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateDirectionVectors();

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

        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalize();

            float accel = onGround ? ACCELERATION : AIR_CONTROL;

            velocity.x += moveDirection.x * accel * deltaTime;
            velocity.z += moveDirection.z * accel * deltaTime;

            float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            if (horizontalSpeed > MOVE_SPEED) {
                float scale = MOVE_SPEED / horizontalSpeed;
                velocity.x *= scale;
                velocity.z *= scale;
            }
        }
        else {
            float friction = onGround ? GROUND_FRICTION : AIR_FRICTION;
            velocity.x -= velocity.x * friction * deltaTime;
            velocity.z -= velocity.z * friction * deltaTime;

            if (Math.abs(velocity.x) < 0.01f) velocity.x = 0;
            if (Math.abs(velocity.z) < 0.01f) velocity.z = 0;
        }

        // Jump
        if (Input.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            jump();
        }
    }

    public void jump() {
        if (onGround) {
            System.out.println("Jump");
            velocity.y = JUMP_FORCE;
            onGround = false;
        }
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

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}