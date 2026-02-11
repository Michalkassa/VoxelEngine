package World;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.lang.Math;

public class ChunkManager {

    private Map<Vector2i,Chunk> chunks;

    public ChunkManager(){
        this.chunks = new HashMap<>();
    }

    public void loadChunk(Vector2i position){
        if(!chunks.containsKey(position)){
            Chunk chunk = new Chunk(position, this);
            chunks.put(position,chunk);

            chunk.buildMesh();

            rebuildAdjacentChunks(position);
        }
    }

    public void unloadChunk(Vector2i position){
        Chunk chunk = chunks.remove(position);
        if(chunk != null){
            chunk.cleanup();
        }
    }

    public Chunk getChunk(Vector2i position){
        return chunks.get(position);
    }


    public boolean isBlockAt(Vector3i world_position){
        Vector2i chunkCoordinates = new Vector2i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y,Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if(chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT){
            return  false;
        }

        try{
            return chunk.getBlock(localCoordinates.x, localCoordinates.y, localCoordinates.z) != 0;
        } catch (RuntimeException e) {
            return false;
        }
    }


    public byte getBlockAt(Vector3i world_position){
        Vector2i chunkCoordinates = new Vector2i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y,Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if(chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT){
            return 0;
        }

        try{
            return chunk.getBlock(localCoordinates.x, localCoordinates.y, localCoordinates.z);
        } catch (RuntimeException e) {
            return 0;
        }
    }


    public void setBlockAt(Vector3i world_position, byte block_type){
        Vector2i chunkCoordinates = new Vector2i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE), Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y,Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if(chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT){
            chunk.setBlock(localCoordinates, block_type);
        }
    }

    public void loadChunksInRadius(Vector2i centre, int radius){
        for (int x = centre.x - radius; x <= centre.x + radius; x++){
            for(int z = centre.y - radius; z <= centre.y + radius; z++){
                loadChunk(new Vector2i(x,z));
            }
        }
    }


    private void rebuildAdjacentChunks(Vector2i position) {
        Vector2i[] neighbors = {
                new Vector2i(position.x + 1, position.y),  // East
                new Vector2i(position.x - 1, position.y),  // West
                new Vector2i(position.x, position.y + 1),  // North
                new Vector2i(position.x, position.y - 1)   // South
        };

        for (Vector2i neighbor : neighbors) {
            Chunk chunk = getChunk(neighbor);
            if (chunk != null) {
                chunk.buildMesh();
            }
        }
    }

    public void renderChunks(){
        for(Chunk chunk : chunks.values()){
            chunk.render();
        }
    }

    public void cleanup() {
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
    }
}
