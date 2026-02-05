package Entity;

import World.Chunk;
import Core.Game;
import org.joml.Vector3f;

public class Entity {
    private static final float GRAVITY_CONSTANT = 9.81f;
    protected Vector3f position;
    protected Vector3f velocity;

    protected float width;
    protected float height;
    protected boolean onGround = false;

    public void update(float dt , Chunk chunk){
        applyCollisions(chunk);
        move(new Vector3f(velocity).mul(dt),chunk);
        applyGravity(dt);
    }

    protected void move(Vector3f velocity, Chunk chunk){
        position.add(velocity);
        applyCollisions(chunk);
    }

    protected void applyCollisions(Chunk chunk){

    }

    protected void applyGravity(float dt){
        if (!onGround){
            velocity.y -= GRAVITY_CONSTANT * dt;
        }
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(Vector3f position){
       this.position = position;
       this. velocity = new Vector3f(0,0,0);
       onGround = false;
    }
}
