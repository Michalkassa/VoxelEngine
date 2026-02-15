package Core;

import World.Chunk;
import org.joml.Vector3f;
import java.lang.Math;
import static org.lwjgl.glfw.GLFW.*;
import org.joml.*;

public class Camera {

    public Vector3f position;
    public Vector3f target;

    private final float CAMERA_MOVEMENT_SPEED = 10f;
    private final float CAMERA_SENSITIVITY = 0.001f;

    private float camera_yaw = (float) -Math.PI/2;
    private float camera_pitch = 0f;

    public Vector3f direction;
    public Vector3f up;
    public Vector3f right;
    private final static Vector3f world_up = new Vector3f(0,1,0);


    public Camera(Vector3f positon, Vector3f target){
        this.position = positon;
        this.target = target;
        this.direction = new Vector3f();
        this.up = new Vector3f();
        this.right = new Vector3f();
    }

    private void movement(float dt){

        if (Input.isKeyDown(GLFW_KEY_W)){
            position.add(new Vector3f(direction).mul(CAMERA_MOVEMENT_SPEED * dt));
        }
        if (Input.isKeyDown(GLFW_KEY_S)){
            position.sub(new Vector3f(direction).mul(CAMERA_MOVEMENT_SPEED * dt));
        }
        if (Input.isKeyDown(GLFW_KEY_A)){
            position.sub(new Vector3f(right).mul(CAMERA_MOVEMENT_SPEED * dt));
        }
        if (Input.isKeyDown(GLFW_KEY_D)){
            position.add(new Vector3f(right).mul(CAMERA_MOVEMENT_SPEED * dt));
        }
        if (Input.isKeyDown(GLFW_KEY_SPACE)){
            position.add(new Vector3f(world_up).mul(CAMERA_MOVEMENT_SPEED * dt));
        }
        if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT)){
            position.sub(new Vector3f(world_up).mul(CAMERA_MOVEMENT_SPEED * dt));
        }

    }

    private void setDirection(float dt){
        camera_yaw += Input.getMouseDeltaX() * CAMERA_SENSITIVITY ;
        camera_pitch -= Input.getMouseDeltaY() * CAMERA_SENSITIVITY ;

        if (camera_pitch > 89) camera_pitch = 89;
        if (camera_pitch < -89) camera_pitch = -89;


        float limit = (float) Math.toRadians(89.0f);
        if (camera_pitch > limit) camera_pitch = limit;
        if (camera_pitch < -limit) camera_pitch = -limit;


        direction.x = (float) (Math.cos(camera_pitch) * Math.cos(camera_yaw));
        direction.y = (float) (Math.sin(camera_pitch));
        direction.z = (float) (Math.cos(camera_pitch) * Math.sin(camera_yaw));


        direction.normalize();
    }

    public void update(float dt){
        movement(dt);
        setDirection(dt);
        right.set(direction).cross(world_up).normalize();
        up.set(right).cross(direction).normalize();
    }


    public Vector3i getChunkPosition(){
        return new Vector3i((int) position.x / Chunk.CHUNK_SIZE, 0 , (int) position.z / Chunk.CHUNK_SIZE);
    }

    public Matrix4f getViewMatrix(Matrix4f dest) {
        return dest.setLookAt(
                position,
                new Vector3f(position).add(direction),
                up
        );
    }

}
