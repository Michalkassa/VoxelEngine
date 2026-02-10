package World;

import Core.Mesh;
import Core.TextureAtlas;
import org.joml.Vector3f;
import org.joml.Vector3i;


import java.util.ArrayList;

public class Chunk {

    private byte[][][] blocks;
    private Mesh ChunkMesh;
    private Vector3f chunkPosition;

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
            {  0.5f, -0.5f,  0.5f },  // BL (facing +X direction)
            {  0.5f,  0.5f,  0.5f },  // TL
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f,  0.5f, -0.5f },  // TR
            {  0.5f, -0.5f, -0.5f },  // BR
            {  0.5f, -0.5f,  0.5f }   // BL
    };

    private static final float[][] CUBE_SOUTH_FACE = {
            { -0.5f, -0.5f, -0.5f },  // BL (facing -X direction)
            { -0.5f,  0.5f, -0.5f },  // TL
            { -0.5f,  0.5f,  0.5f },  // TR
            { -0.5f,  0.5f,  0.5f },  // TR
            { -0.5f, -0.5f,  0.5f },  // BR
            { -0.5f, -0.5f, -0.5f }   // BL
    };

    private static final float[][] CUBE_EAST_FACE = {
            { -0.5f, -0.5f,  0.5f },  // BL (facing +Z direction)
            { -0.5f,  0.5f,  0.5f },  // TL
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f,  0.5f,  0.5f },  // TR
            {  0.5f, -0.5f,  0.5f },  // BR
            { -0.5f, -0.5f,  0.5f }   // BL
    };

    private static final float[][] CUBE_WEST_FACE = {
            {  0.5f, -0.5f, -0.5f },  // BL (facing -Z direction)
            {  0.5f,  0.5f, -0.5f },  // TL
            { -0.5f,  0.5f, -0.5f },  // TR
            { -0.5f,  0.5f, -0.5f },  // TR
            { -0.5f, -0.5f, -0.5f },  // BR
            {  0.5f, -0.5f, -0.5f }   // BL
    };


    private ArrayList<Float> vertices;


    public Chunk(Vector3f position){
        this.chunkPosition = position;
        this.blocks = new byte[CHUNK_SIZE+1][CHUNK_HEIGHT+1][CHUNK_SIZE+1];
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

    public static int worldToLocalX(int worldX) {
        int localX = worldX % CHUNK_SIZE;
        if(localX < 0) localX += CHUNK_SIZE;
        return localX;
    }

    public static int worldToLocalZ(int worldZ) {
        int localZ = worldZ % CHUNK_SIZE;
        if(localZ < 0) localZ += CHUNK_SIZE;
        return localZ;
    }

    public static int worldToChunkX(int worldX) {
        return Math.floorDiv(worldX, CHUNK_SIZE);
    }

    public static int worldToChunkZ(int worldZ) {
        return Math.floorDiv(worldZ, CHUNK_SIZE);
    }

     private void generateBlocks(){

        for(int x = 0 ; x < CHUNK_SIZE; x++){
            for(int y = 0 ; y < CHUNK_HEIGHT; y++){
                for(int z = 0 ; z < CHUNK_SIZE; z++){
                    if(y < 1){
                        blocks[x][y][z] = 1;
                    }
                }
            }
        }
    }

    private void buildMesh(){
        vertices = new ArrayList<>();

        float worldX =  chunkPosition.x * CHUNK_SIZE;
        float worldY = chunkPosition.y * CHUNK_HEIGHT;
        float worldZ = chunkPosition.z * CHUNK_SIZE;

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

    //TODO fix repetition
    private void addBlock(Vector3f position , Vector3i relativeChunkPosition){

        int x = relativeChunkPosition.x;
        int y = relativeChunkPosition.y;
        int z = relativeChunkPosition.z;

        if (y < CHUNK_HEIGHT && blocks[x][y+1][z] == 0){
            for (int i = 0; i < CUBE_TOP_FACE.length; i++) {
                vertices.add(CUBE_TOP_FACE[i][0] + position.x);
                vertices.add(CUBE_TOP_FACE[i][1] + position.y);
                vertices.add(CUBE_TOP_FACE[i][2] + position.z);

                vertices.add(topTextureMapping[i][0]);
                vertices.add(topTextureMapping[i][1]);
            }
        }

        if (y == 0 || (y > 0 && blocks[x][y-1][z] == 0)){
            for (int i = 0; i < CUBE_BOTTOM_FACE.length; i++) {
                vertices.add(CUBE_BOTTOM_FACE[i][0] + position.x);
                vertices.add(CUBE_BOTTOM_FACE[i][1] + position.y);
                vertices.add(CUBE_BOTTOM_FACE[i][2] + position.z);

                vertices.add(bottomTextureMapping[i][0]);
                vertices.add(bottomTextureMapping[i][1]);
            }
        }

        if (x < CHUNK_SIZE && blocks[x+1][y][z] == 0){
            for (int i = 0; i < CUBE_NORTH_FACE.length; i++) {
                vertices.add(CUBE_NORTH_FACE[i][0] + position.x);
                vertices.add(CUBE_NORTH_FACE[i][1] + position.y);
                vertices.add(CUBE_NORTH_FACE[i][2] + position.z);

                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        if (x == 0 || (x > 0 && blocks[x-1][y][z] == 0)){
            for (int i = 0; i < CUBE_SOUTH_FACE.length; i++) {
                vertices.add(CUBE_SOUTH_FACE[i][0] + position.x);
                vertices.add(CUBE_SOUTH_FACE[i][1] + position.y);
                vertices.add(CUBE_SOUTH_FACE[i][2] + position.z);

                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        if (z < CHUNK_SIZE && blocks[x][y][z+1] == 0){
            for (int i = 0; i < CUBE_EAST_FACE.length; i++) {
                vertices.add(CUBE_EAST_FACE[i][0] + position.x);
                vertices.add(CUBE_EAST_FACE[i][1] + position.y);
                vertices.add(CUBE_EAST_FACE[i][2] + position.z);

                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }

        if (z == 0 || (z > 0 && blocks[x][y][z-1] == 0)) {

            for (int i = 0; i < CUBE_WEST_FACE.length; i++) {
                vertices.add(CUBE_WEST_FACE[i][0] + position.x);
                vertices.add(CUBE_WEST_FACE[i][1] + position.y);
                vertices.add(CUBE_WEST_FACE[i][2] + position.z);

                vertices.add(sideTextureMapping[i][0]);
                vertices.add(sideTextureMapping[i][1]);
            }
        }
    }


    public void render(){
        ChunkMesh.draw();
    }

    public void cleanup(){
        ChunkMesh.cleanup();
    }

}
