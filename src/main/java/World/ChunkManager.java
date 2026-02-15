package World;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.lang.Math;

public class ChunkManager {

    private Map<Vector3i,Chunk> chunks;

    public ChunkManager(){
        this.chunks = new HashMap<>();
    }

    public void loadChunk(Vector3i position){
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

    public Chunk getChunk(Vector3f world_position) {
        int chunkX = Math.floorDiv((int) world_position.x, Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv((int) world_position.z, Chunk.CHUNK_SIZE);

        return getChunk(new Vector3i(chunkX,0, chunkZ));
    }

    public Chunk getChunk(Vector3i position){
        return chunks.get(position);
    }


    public boolean isBlockAt(Vector3i world_position){
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE),0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
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
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE),0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
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
        Vector3i chunkCoordinates = new Vector3i(Math.floorDiv(world_position.x, Chunk.CHUNK_SIZE),0, Math.floorDiv(world_position.z, Chunk.CHUNK_SIZE));
        Vector3i localCoordinates = new Vector3i(Math.floorMod(world_position.x, Chunk.CHUNK_SIZE), world_position.y,Math.floorMod(world_position.z, Chunk.CHUNK_SIZE));

        Chunk chunk = getChunk(chunkCoordinates);

        if(chunk == null || localCoordinates.y < 0 || localCoordinates.y >= Chunk.CHUNK_HEIGHT){
            throw new RuntimeException("Trying to set block that doesnt Exist");
        }
        chunk.setBlock(localCoordinates, block_type);
        rebuildAdjacentChunks(chunkCoordinates);
    }

    public void loadChunksInRadius(Vector2i centre, int radius){
        for (int x = centre.x - radius; x <= centre.x + radius; x++){
            for(int z = centre.y - radius; z <= centre.y + radius; z++){
                loadChunk(new Vector3i(x,0,z));
            }
        }
    }


    private void rebuildAdjacentChunks(Vector3i position) {
        Vector3i[] neighbors = {
                new Vector3i(position.x + 1, 0, position.z),  // North (X+)
                new Vector3i(position.x - 1, 0, position.z),  // South (X-)
                new Vector3i(position.x, 0, position.z + 1),  // East (Z+)
                new Vector3i(position.x, 0, position.z - 1)   // West (Z-)
        };

        for (Vector3i neighbor : neighbors) {
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
