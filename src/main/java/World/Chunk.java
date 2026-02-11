package World;

import Core.Mesh;
import Core.TextureAtlas;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;


import java.util.ArrayList;
import java.util.Random;

public class Chunk {
    private byte[][][] blocks;
    private Mesh ChunkMesh;
    private Vector2i chunkPosition;
    private ChunkManager chunkManager;

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 128;

    private float[][] topTextureMapping;
    private float[][] bottomTextureMapping;
    private float[][] sideTextureMapping;

    private static final float[][] CUBE_TOP_FACE = {
            { -0.5f,  0.5f, -0.5f },  // BL (looking down at top)
            { -0.5f,  0.5f,  0.5f },  // TL
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f, -0.5f },  // BR
            { -0.5f,  0.5f, -0.5f }   // BL
    };

    private static final float[][] CUBE_BOTTOM_FACE = {
            { -0.5f, -0.5f,  0.5f },  // BL (looking up at bottom)
            { -0.5f, -0.5f, -0.5f },  // TL
            {  0.5f, -0.5f, -0.5f },  // TR
            {  0.5f, -0.5f, -0.5f },  // TR
            {  0.5f, -0.5f,  0.5f },  // BR
            { -0.5f, -0.5f,  0.5f }   // BL
    };

    private static final float[][] CUBE_NORTH_FACE = {
            {  0.5f, -0.5f,  0.5f },  // BL
            {  0.5f, -0.5f, -0.5f },  // BR
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TL
            {  0.5f, -0.5f,  0.5f }   // BL
    };

    private static final float[][] CUBE_SOUTH_FACE = {
            { -0.5f, -0.5f, -0.5f },  // BR
            { -0.5f, -0.5f,  0.5f },  // BL
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f,  0.5f, -0.5f },  // TR
            { -0.5f, -0.5f, -0.5f }   // BR
    };

    private static final float[][] CUBE_EAST_FACE = {
            { -0.5f, -0.5f,  0.5f },  // BL
            {  0.5f, -0.5f,  0.5f },  // BR
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TR
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f, -0.5f,  0.5f }   // BL
    };

    private static final float[][] CUBE_WEST_FACE = {
            {  0.5f, -0.5f, -0.5f },  // BR
            { -0.5f, -0.5f, -0.5f },  // BL
            { -0.5f,  0.5f, -0.5f },  // TL
            { -0.5f,  0.5f, -0.5f },  // TL
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f, -0.5f, -0.5f }   // BR
    };


    private ArrayList<Float> vertices;


    public Chunk(Vector2i position, ChunkManager chunkManager){
        this.chunkPosition = position;
        this.blocks = new byte[CHUNK_SIZE+1][CHUNK_HEIGHT+1][CHUNK_SIZE+1];
        this.chunkManager = chunkManager;
        generateBlocks();
        buildMesh();
    }

    public byte getBlock(int x, int y, int z){
        if(x > CHUNK_SIZE || x < 0){
            throw new RuntimeException("Accessing x coordinate outside the chunk");
        }
        if(y > CHUNK_HEIGHT || y < 0){
            throw new RuntimeException("Accessing y coordinate outside the chunk");
        }
        if(z > CHUNK_SIZE || z < 0){
            throw new RuntimeException("Accessing z coordinate outside the chunk");
        }

        return blocks[x][y][z];
    }

    private void generateBlocks(){
        for(int x = 0; x < CHUNK_SIZE; x++){
            for(int z = 0; z < CHUNK_SIZE; z++){
                for(int y = 0; y < 5; y++){
                    blocks[x][y][z] = 1;
                }
            }
        }
    }

    public void buildMesh(){
        vertices = new ArrayList<>();

        float worldX =  chunkPosition.x * CHUNK_SIZE;
        float worldY = 0;
        float worldZ = chunkPosition.y * CHUNK_SIZE;

        for(int x = 0 ; x < CHUNK_SIZE; x++){
            for(int y = 0 ; y < CHUNK_HEIGHT; y++){
                for(int z = 0 ; z < CHUNK_SIZE; z++){
                    if(blocks[x][y][z] != 0){
                        //TEMPORARY , TODO fix
                        topTextureMapping = TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.GRASS);
                        bottomTextureMapping = TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.GRASS);;
                        sideTextureMapping = TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.GRASS);;
                        addBlock(new Vector3f(x + worldX,y + worldY, z + worldZ), new Vector3i(x,y,z));
                    }
                }
            }
        }

        float[] data = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            data[i] = vertices.get(i);
        }

        ChunkMesh = new Mesh(data, data.length / 5);

    }

    private boolean isBlockAtWorldPos(int worldX, int worldY, int worldZ) {
        if (chunkManager == null) {
            return false;
        }
        return chunkManager.isBlockAt(new Vector3i(worldX, worldY, worldZ));
    }


    //TODO fix repetition
    private void addBlock(Vector3f position, Vector3i relativeChunkPosition){
        int x = relativeChunkPosition.x;
        int y = relativeChunkPosition.y;
        int z = relativeChunkPosition.z;

        int worldX = chunkPosition.x * CHUNK_SIZE + x;
        int worldY = y;
        int worldZ = chunkPosition.y * CHUNK_SIZE + z;

        boolean renderTop;
        if (y < CHUNK_HEIGHT - 1) {
            renderTop = blocks[x][y+1][z] == 0;
        } else {
            renderTop = !isBlockAtWorldPos(worldX, worldY + 1, worldZ);
        }
        if (renderTop) {
            for (int i = 0; i < CUBE_TOP_FACE.length; i++) {
                vertices.add(CUBE_TOP_FACE[i][0] + position.x);
                vertices.add(CUBE_TOP_FACE[i][1] + position.y);
                vertices.add(CUBE_TOP_FACE[i][2] + position.z);
                vertices.add(topTextureMapping[i][0]);
                vertices.add(topTextureMapping[i][1]);
            }
        }

        boolean renderBottom;
        if (y == 0){
            renderBottom = false;
        }
        else if (y > 0) {
            renderBottom = blocks[x][y-1][z] == 0;
        } else {
            renderBottom = !isBlockAtWorldPos(worldX, worldY - 1, worldZ);
        }
        if (renderBottom) {
            for (int i = 0; i < CUBE_BOTTOM_FACE.length; i++) {
                vertices.add(CUBE_BOTTOM_FACE[i][0] + position.x);
                vertices.add(CUBE_BOTTOM_FACE[i][1] + position.y);
                vertices.add(CUBE_BOTTOM_FACE[i][2] + position.z);
                vertices.add(bottomTextureMapping[i][0]);
                vertices.add(bottomTextureMapping[i][1]);
            }
        }

        boolean renderNorth;
        if (x < CHUNK_SIZE - 1) {
            renderNorth = blocks[x+1][y][z] == 0;
        } else {
            renderNorth = !isBlockAtWorldPos(worldX + 1, worldY, worldZ);
        }
        if (renderNorth) {
            for (int i = 0; i < CUBE_NORTH_FACE.length; i++) {
                vertices.add(CUBE_NORTH_FACE[i][0] + position.x);
                vertices.add(CUBE_NORTH_FACE[i][1] + position.y);
                vertices.add(CUBE_NORTH_FACE[i][2] + position.z);
                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        boolean renderSouth;
        if (x > 0) {
            renderSouth = blocks[x-1][y][z] == 0;
        } else {
            renderSouth = !isBlockAtWorldPos(worldX - 1, worldY, worldZ);
        }
        if (renderSouth) {
            for (int i = 0; i < CUBE_SOUTH_FACE.length; i++) {
                vertices.add(CUBE_SOUTH_FACE[i][0] + position.x);
                vertices.add(CUBE_SOUTH_FACE[i][1] + position.y);
                vertices.add(CUBE_SOUTH_FACE[i][2] + position.z);
                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        boolean renderEast;
        if (z < CHUNK_SIZE - 1) {
            renderEast = blocks[x][y][z+1] == 0;
        } else {
            renderEast = !isBlockAtWorldPos(worldX, worldY, worldZ + 1);
        }
        if (renderEast) {
            for (int i = 0; i < CUBE_EAST_FACE.length; i++) {
                vertices.add(CUBE_EAST_FACE[i][0] + position.x);
                vertices.add(CUBE_EAST_FACE[i][1] + position.y);
                vertices.add(CUBE_EAST_FACE[i][2] + position.z);
                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        boolean renderWest;
        if (z > 0) {
            renderWest = blocks[x][y][z-1] == 0;
        } else {
            renderWest = !isBlockAtWorldPos(worldX, worldY, worldZ - 1);
        }
        if (renderWest) {
            for (int i = 0; i < CUBE_WEST_FACE.length; i++) {
                vertices.add(CUBE_WEST_FACE[i][0] + position.x);
                vertices.add(CUBE_WEST_FACE[i][1] + position.y);
                vertices.add(CUBE_WEST_FACE[i][2] + position.z);
                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }
    }

    public void setBlock(Vector3i relative_chunk_position, byte block_type){
        int x = relative_chunk_position.x;
        int y = relative_chunk_position.y;
        int z = relative_chunk_position.z;

        if(x > CHUNK_SIZE || x < 0){
            throw new RuntimeException("Accessing x coordinate outside the chunk");
        }
        if(y > CHUNK_HEIGHT || y < 0){
            throw new RuntimeException("Accessing y coordinate outside the chunk");
        }
        if(z > CHUNK_SIZE || z < 0){
            throw new RuntimeException("Accessing z coordinate outside the chunk");
        }

        blocks[x][y][z] = block_type;
        buildMesh();
    }

    public void render(){
        ChunkMesh.draw();
    }

    public void cleanup(){
        ChunkMesh.cleanup();
    }

}
