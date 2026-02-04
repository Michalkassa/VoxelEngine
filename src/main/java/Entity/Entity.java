package Entity;

import World.Chunk;
import org.joml.Vector3f;

public class Entity {
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
        float minX = position.x - width/2;
        float maxX = position.x + width/2;
        float minY = position.y;
        float maxY = position.y + height;
        float minZ = position.z - width/2;
        float maxZ = position.z + width/2;

        onGround = false;

        int startX = (int) Math.floor(minX);
        int endX   = (int) Math.floor(maxX);
        int startY = (int) Math.floor(minY);
        int endY   = (int) Math.floor(maxY);
        int startZ = (int) Math.floor(minZ);
        int endZ   = (int) Math.floor(maxZ);

        for(int x = startX; x <= endX; x++){
            for(int y = startY; y <= endY; y++){
                for(int z = startZ; z <= endZ; z++){
                    if(chunk.getBlock(x, y, z) != 0){
                        if(velocity.y < 0 && minY < y + 1 && maxY > y){
                            position.y = y + 1;
                            velocity.y = 0;
                            onGround = true;
                            minY = position.y;
                            maxY = position.y + height;
                        } else if(velocity.y > 0 && maxY > y && minY < y + 1){
                            position.y = y - height;
                            velocity.y = 0;
                            maxY = position.y + height;
                            minY = position.y;
                        }

                        if(velocity.x != 0){
                            if(velocity.x > 0 && maxX > x && minX < x + 1){
                                position.x = x - width/2;
                                velocity.x = 0;
                                minX = position.x - width/2;
                                maxX = position.x + width/2;
                            } else if(velocity.x < 0 && minX < x + 1 && maxX > x){
                                position.x = x + 1 + width/2;
                                velocity.x = 0;
                                minX = position.x - width/2;
                                maxX = position.x + width/2;
                            }
                        }

                        if(velocity.z != 0){
                            if(velocity.z > 0 && maxZ > z && minZ < z + 1){
                                position.z = z - width/2;
                                velocity.z = 0;
                                minZ = position.z - width/2;
                                maxZ = position.z + width/2;
                            } else if(velocity.z < 0 && minZ < z + 1 && maxZ > z){
                                position.z = z + 1 + width/2;
                                velocity.z = 0;
                                minZ = position.z - width/2;
                                maxZ = position.z + width/2;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void applyGravity(float dt){
        if (!onGround){
            velocity.y -= 9.81 * dt;
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
