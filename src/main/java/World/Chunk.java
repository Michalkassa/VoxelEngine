package World;

import Core.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Chunk {

    private byte[][][] blocks;
    private Mesh ChunkMesh;
    private Vector3f chunkPosition;

    private int width;
    private int height;

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
        this.width = 16;
        this.height = 128;
        this.blocks = new byte[width][height][width];
        generateBlocks();
        buildMesh();
    }

//    public byte publicGetBlock(Vector3f position){
//        return
//    }

    public void setBlock(Vector3f position, byte blockId){

    }

     private void generateBlocks(){

        for(int x = 0 ; x < width; x++){
            for(int y = 0 ; y < height; y++){
                for(int z = 0 ; z < width; z++){
                    if(y < 1){
                        blocks[x][y][z] = 1;
                    }
                }
            }
        }
    }

    private void buildMesh(){
        vertices = new ArrayList<>();

        float worldX =  chunkPosition.x * width;
        float worldY = chunkPosition.y * height;
        float worldZ = chunkPosition.z * width;

        for(int x = 0 ; x < width; x++){
            for(int y = 0 ; y < height; y++){
                for(int z = 0 ; z < width; z++){
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
