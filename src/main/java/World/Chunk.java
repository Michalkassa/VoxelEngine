package World;

import Core.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Chunk {

    private byte[][][] blocks;
    private Mesh ChunkMesh;
    private Vector3f chunkPosition;

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 128;

    private static final float[] CUBE = {
            // FRONT FACE (z = 0.5)
            -0.5f, -0.5f,  0.5f, 0f, 0f,  // BL
            0.5f, -0.5f,  0.5f, 1f, 0f,  // BR
            0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            -0.5f,  0.5f,  0.5f, 0f, 1f,  // TL
            -0.5f, -0.5f,  0.5f, 0f, 0f,  // BL

            // BACK FACE (z = -0.5)
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL
            -0.5f,  0.5f, -0.5f, 0f, 1f,  // TL
            0.5f,  0.5f, -0.5f, 1f, 1f,  // TR
            0.5f,  0.5f, -0.5f, 1f, 1f,  // TR
            0.5f, -0.5f, -0.5f, 1f, 0f,  // BR
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL

            // LEFT FACE (x = -0.5)
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL
            -0.5f, -0.5f,  0.5f, 1f, 0f,  // BR
            -0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            -0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            -0.5f,  0.5f, -0.5f, 0f, 1f,  // TL
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL

            // RIGHT FACE (x = 0.5)
            0.5f, -0.5f,  0.5f, 0f, 0f,  // BL
            0.5f,  0.5f,  0.5f, 0f, 1f,  // TL
            0.5f,  0.5f, -0.5f, 1f, 1f,  // TR
            0.5f,  0.5f, -0.5f, 1f, 1f,  // TR
            0.5f, -0.5f, -0.5f, 1f, 0f,  // BR
            0.5f, -0.5f,  0.5f, 0f, 0f,  // BL

            // BOTTOM FACE (y = -0.5)
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL
            0.5f, -0.5f, -0.5f, 1f, 0f,  // BR
            0.5f, -0.5f,  0.5f, 1f, 1f,  // TR
            0.5f, -0.5f,  0.5f, 1f, 1f,  // TR
            -0.5f, -0.5f,  0.5f, 0f, 1f,  // TL
            -0.5f, -0.5f, -0.5f, 0f, 0f,  // BL

            // TOP FACE (y = 0.5)
            -0.5f,  0.5f, -0.5f, 0f, 0f,  // BL
            -0.5f,  0.5f,  0.5f, 0f, 1f,  // TL
            0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            0.5f,  0.5f,  0.5f, 1f, 1f,  // TR
            0.5f,  0.5f, -0.5f, 1f, 0f,  // BR
            -0.5f,  0.5f, -0.5f, 0f, 0f   // BL
    };
    private ArrayList<Float> vertices;


    public Chunk(Vector3f position){
        this.chunkPosition = position;
        this.blocks = new byte[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
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
                        addBlock(new Vector3f(x + worldX,y + worldY, z + worldZ));
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

    private void addBlock(Vector3f position){
        for (int i = 0; i < CUBE.length; i += 5) {

            vertices.add(CUBE[i]     + position.x);
            vertices.add(CUBE[i + 1] + position.y);
            vertices.add(CUBE[i + 2] + position.z);

            vertices.add(CUBE[i + 3]);
            vertices.add(CUBE[i + 4]);
        }
    }

    public void render(){
        ChunkMesh.draw();
    }

    public void cleanup(){
        ChunkMesh.cleanup();
    }

}
