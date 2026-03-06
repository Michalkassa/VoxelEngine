package Entity;

import Core.Mesh;
import Core.Transform;
import World.Chunk;
import Core.Game;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public abstract class Entity {
    private static final float GRAVITY_CONSTANT = 9.81f;
    protected Transform transform;
    protected Vector3f velocity;

    protected Mesh mesh;

    protected boolean onGround = false;

    protected Entity(Transform transform){
        this.transform = transform;
        this.velocity = new Vector3f(0,0,0);

    }

    public void update(float dt , Chunk chunk){
        applyGravity(dt);
        move(new Vector3f(velocity).mul(dt),chunk);
        applyCollisions(chunk);
    }

    protected void move(Vector3f velocity, Chunk chunk){
        transform.position.add(velocity);
        applyCollisions(chunk);
    }

    protected void applyCollisions(Chunk chunk) {
        //TODO fix

        float groundLevel = 3f;

        if (transform.position.y <= groundLevel && velocity.y <= 0f) {
            transform.position.y = groundLevel;
            velocity.y = 0f;
            onGround = true;
        }
    }

    protected void applyGravity(float dt){
        if (!onGround){
            velocity.y -= GRAVITY_CONSTANT * dt;
        }
    }

    public void render(){
        mesh.draw();
    }

    public Vector3f getPosition() {
        return new Vector3f(transform.position);
    }

    public void setPosition(Vector3f position){
        transform.position= position;
       velocity = new Vector3f(0,0,0);
       onGround = false;
    }

    public void cleanup(){
        mesh.cleanup();
    }
}
