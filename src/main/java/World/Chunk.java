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
    private Vector3i chunkPosition;
    public ChunkManager chunkManager;

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 128;

    private float[][] topTextureMapping;
    private float[][] bottomTextureMapping;
    private float[][] sideTextureMapping;

    // Top face (Y = +0.5, normal pointing up +Y)
    private static final float[][] CUBE_TOP_FACE = {
            { -0.5f,  0.5f, -0.5f },  // BL
            { -0.5f,  0.5f,  0.5f },  // TL
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f, -0.5f },  // BR
            { -0.5f,  0.5f, -0.5f }   // BL
    };

    // Bottom face (Y = -0.5, normal pointing down -Y)
    private static final float[][] CUBE_BOTTOM_FACE = {
            { -0.5f, -0.5f,  0.5f },  // BL
            { -0.5f, -0.5f, -0.5f },  // TL
            {  0.5f, -0.5f, -0.5f },  // TR
            {  0.5f, -0.5f, -0.5f },  // TR
            {  0.5f, -0.5f,  0.5f },  // BR
            { -0.5f, -0.5f,  0.5f }   // BL
    };

    // North face (X = +0.5, normal pointing in +X direction)
    private static final float[][] CUBE_NORTH_FACE = {
            {  0.5f, -0.5f,  0.5f },  // BL
            {  0.5f, -0.5f, -0.5f },  // BR
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TL
            {  0.5f, -0.5f,  0.5f }   // BL
    };

    // South face (X = -0.5, normal pointing in -X direction)
    private static final float[][] CUBE_SOUTH_FACE = {
            { -0.5f, -0.5f, -0.5f },  // BR
            { -0.5f, -0.5f,  0.5f },  // BL
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f,  0.5f, -0.5f },  // TR
            { -0.5f, -0.5f, -0.5f }   // BR
    };

    // East face (Z = +0.5, normal pointing in +Z direction)
    private static final float[][] CUBE_EAST_FACE = {
            { -0.5f, -0.5f,  0.5f },  // BL
            {  0.5f, -0.5f,  0.5f },  // BR
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TR
            { -0.5f,  0.5f,  0.5f },  // TL
            { -0.5f, -0.5f,  0.5f }   // BL
    };

    // West face (Z = -0.5, normal pointing in -Z direction)
    private static final float[][] CUBE_WEST_FACE = {
            {  0.5f, -0.5f, -0.5f },  // BR
            { -0.5f, -0.5f, -0.5f },  // BL
            { -0.5f,  0.5f, -0.5f },  // TL
            { -0.5f,  0.5f, -0.5f },  // TL
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f, -0.5f, -0.5f }   // BR
    };
    private ArrayList<Float> vertices;


    public Chunk(Vector3i position, ChunkManager chunkManager, byte[][][] loadedBlocks){
        this.chunkPosition = position;
        this.blocks = loadedBlocks;
        this.chunkManager = chunkManager;

        topTextureMapping = TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.GRASS);
        bottomTextureMapping = TextureAtlas.bottomTextureMapping(TextureAtlas.BlockTextures.GRASS);
        sideTextureMapping = TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.GRASS);

        buildMesh();
    }

    public byte getBlock(int x, int y, int z){
        if(x >= CHUNK_SIZE || x < 0){
            throw new RuntimeException("Accessing x coordinate outside the chunk");
        }
        if(y >= CHUNK_HEIGHT || y < 0){
            throw new RuntimeException("Accessing y coordinate outside the chunk");
        }
        if(z >= CHUNK_SIZE || z < 0){
            throw new RuntimeException("Accessing z coordinate outside the chunk");
        }

        return blocks[x][y][z];
    }

//    private void generateBlocks(){
//        for(int x = 0; x < CHUNK_SIZE; x++){
//            for(int z = 0; z < CHUNK_SIZE; z++){
//                for(int y = 0; y < 2; y++){
//                    blocks[x][y][z] = 1;
//                }
//            }
//        }
//    }


    public void buildMesh(){
        vertices = new ArrayList<>();

        float worldX =  chunkPosition.x * CHUNK_SIZE;
        float worldY = 0;
        float worldZ = chunkPosition.z * CHUNK_SIZE;

        for(int x = 0 ; x < CHUNK_SIZE; x++){
            for(int y = 0 ; y < CHUNK_HEIGHT; y++){
                for(int z = 0 ; z < CHUNK_SIZE; z++){
                    if(blocks[x][y][z] != 0){
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

    private void addFace(float[][] face, float[][] texMapping, float px, float py, float pz) {
        for (int i = 0; i < 6; i++) {
            vertices.add(face[i][0] + px);
            vertices.add(face[i][1] + py);
            vertices.add(face[i][2] + pz);
            vertices.add(texMapping[i][0]);
            vertices.add(texMapping[i][1]);
        }
    }


    private void addBlock(Vector3f position, Vector3i relativeChunkPosition){
        int x = relativeChunkPosition.x;
        int y = relativeChunkPosition.y;
        int z = relativeChunkPosition.z;

        int worldX = chunkPosition.x * CHUNK_SIZE + x;
        int worldY = y;
        int worldZ = chunkPosition.z * CHUNK_SIZE + z;

        boolean hasTop = !isBlockAtWorldPos(worldX, worldY + 1, worldZ);
        boolean hasBottom = !isBlockAtWorldPos(worldX, worldY - 1, worldZ);
        boolean hasNorth = !isBlockAtWorldPos(worldX + 1, worldY, worldZ);
        boolean hasSouth = !isBlockAtWorldPos(worldX - 1, worldY, worldZ);
        boolean hasEast = !isBlockAtWorldPos(worldX, worldY, worldZ + 1);
        boolean hasWest = !isBlockAtWorldPos(worldX, worldY, worldZ - 1);

        if (!hasTop && !hasBottom && !hasNorth && !hasSouth && !hasEast && !hasWest) {
            return;
        }

        float px = position.x, py = position.y, pz = position.z;

        if (hasTop) addFace(CUBE_TOP_FACE, topTextureMapping, px, py, pz);
        if (hasBottom) addFace(CUBE_BOTTOM_FACE, bottomTextureMapping, px, py, pz);
        if (hasNorth) addFace(CUBE_NORTH_FACE, sideTextureMapping, px, py, pz);
        if (hasSouth) addFace(CUBE_SOUTH_FACE, sideTextureMapping, px, py, pz);
        if (hasEast) addFace(CUBE_EAST_FACE, sideTextureMapping, px, py, pz);
        if (hasWest) addFace(CUBE_WEST_FACE, sideTextureMapping, px, py, pz);
    }

    public void setBlock(Vector3i relative_chunk_position, byte block_type){
        int x = relative_chunk_position.x;
        int y = relative_chunk_position.y;
        int z = relative_chunk_position.z;

        if(x >= CHUNK_SIZE || x < 0){
            throw new RuntimeException("Accessing x coordinate outside the chunk");
        }
        if(y >= CHUNK_HEIGHT || y < 0){
            throw new RuntimeException("Accessing y coordinate outside the chunk");
        }
        if(z >= CHUNK_SIZE || z < 0){
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
