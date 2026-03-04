package Entity;

import org.joml.Vector3f;

public interface Playable {


    void handleInput(float deltaTime);

    Vector3f getRotation();

    void setRotation(float yaw, float pitch);

    void jump();

    Vector3f getForward();

    Vector3f getRight();
}