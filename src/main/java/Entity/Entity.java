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
    protected Collider collider;

    protected Mesh mesh;

    protected boolean onGround = false;

    protected Entity(Transform transform){
        this.transform = transform;
        this.velocity = new Vector3f(0,0,0);
        this.collider = new Collider(transform);

    }

    public void update(float dt , Chunk chunk){
        applyCollisions(chunk);
        move(new Vector3f(velocity).mul(dt),chunk);
        applyGravity(dt);
    }

    protected void move(Vector3f velocity, Chunk chunk){
        transform.position.add(velocity);
        applyCollisions(chunk);
    }

    protected void applyCollisions(Chunk chunk) {
       if(transform.position.y <=6){
            velocity.y = 0;
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
