package Core;

import World.World;
import org.joml.Vector3f;
import java.lang.Math;
import org.joml.*;

public class Camera {

    public Vector3f position;
    public Vector3f target;

    private float camera_yaw = 0.0f;
    private float camera_pitch = 0.0f;
    private float FOV = 60.0f;
    private Raycast raycast;
    private final float INTERACTION_DISTANCE = 4f;

    //TODO fix to private
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

    public Vector3i shootRaycast(){
        raycast.setDirection(direction);
        raycast.setOrigin(position);
        return raycast.getBlockPosition();
    }

    private void setDirection(float dt){

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
        //movement(dt);
        setDirection(dt);
        right.set(direction).cross(world_up).normalize();
        up.set(right).cross(direction).normalize();
    }

    public Matrix4f getViewMatrix(Matrix4f dest) {
        return dest.setLookAt(
                position,
                new Vector3f(position).add(direction),
                up
        );
    }

    public float getFOV() {
        return FOV;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setYaw(float yaw) {
        camera_yaw = yaw;
    }

    public void setPitch(float pitch) {
       camera_pitch = pitch;
    }

    public void setRaycast(Raycast raycast) {
        this.raycast = raycast;
    }
}
