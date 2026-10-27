package World;

import Core.ChunkMesh;
import org.joml.Vector3i;

import java.util.Arrays;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTION_COUNT = CHUNK_HEIGHT / SECTION_HEIGHT;
    private static final int SECTION_VOLUME = CHUNK_SIZE * SECTION_HEIGHT * CHUNK_SIZE;

    // Sections that contain only air stay null
    private final byte[][] sections = new byte[SECTION_COUNT][];
    private ChunkMesh chunkMesh;
    private boolean meshed = false;
    private int meshMinY = 0;
    private int meshMaxY = 0;
    private final Vector3i chunkPosition;
    public ChunkManager chunkManager;

    private static final ThreadLocal<int[]> SCRATCH = ThreadLocal.withInitial(() -> new int[16384]);

    public Chunk(Vector3i position, ChunkManager chunkManager, byte[][][] loadedBlocks){
        this.chunkPosition = position;
        this.chunkManager = chunkManager;

        int sizeX = Math.min(loadedBlocks.length, CHUNK_SIZE);
        for (int x = 0; x < sizeX; x++) {
            int sizeY = Math.min(loadedBlocks[x].length, CHUNK_HEIGHT);
            for (int y = 0; y < sizeY; y++) {
                int sizeZ = Math.min(loadedBlocks[x][y].length, CHUNK_SIZE);
                for (int z = 0; z < sizeZ; z++) {
                    byte block = loadedBlocks[x][y][z];
                    if (block != 0) {
                        writeBlock(x, y, z, block);
                    }
                }
            }
        }
    }

    private static int index(int x, int y, int z) {
        return ((y & (SECTION_HEIGHT - 1)) << 8) | (z << 4) | x;
    }

    private static void checkBounds(int x, int y, int z) {
        if(x >= CHUNK_SIZE || x < 0){
            throw new RuntimeException("Accessing x coordinate outside the chunk");
        }
        if(y >= CHUNK_HEIGHT || y < 0){
            throw new RuntimeException("Accessing y coordinate outside the chunk");
        }
        if(z >= CHUNK_SIZE || z < 0){
            throw new RuntimeException("Accessing z coordinate outside the chunk");
        }
    }

    byte blockAt(int x, int y, int z) {
        byte[] section = sections[y / SECTION_HEIGHT];
        return section == null ? 0 : section[index(x, y, z)];
    }

    private void writeBlock(int x, int y, int z, byte blockType) {
        int sectionIndex = y / SECTION_HEIGHT;
        byte[] section = sections[sectionIndex];
        if (section == null) {
            if (blockType == 0) return;
            section = new byte[SECTION_VOLUME];
            sections[sectionIndex] = section;
        }
        section[index(x, y, z)] = blockType;
    }

    public byte getBlock(int x, int y, int z){
        checkBounds(x, y, z);
        return blockAt(x, y, z);
    }

    public void setBlock(Vector3i relative_chunk_position, byte block_type){
        int x = relative_chunk_position.x;
        int y = relative_chunk_position.y;
        int z = relative_chunk_position.z;
        checkBounds(x, y, z);
        writeBlock(x, y, z, block_type);
    }

    public boolean isSectionAllocated(int sectionIndex) {
        return sections[sectionIndex] != null;
    }

    private Chunk neighbour(int dx, int dz) {
        if (chunkManager == null) return null;
        return chunkManager.getChunk(new Vector3i(chunkPosition.x + dx, 0, chunkPosition.z + dz));
    }

    private static boolean isSolid(Chunk chunk, int x, int y, int z) {
        return chunk != null && chunk.blockAt(x, y, z) != 0;
    }

    private boolean isNeighbourSolid(int x, int y, int z, Chunk north, Chunk south, Chunk east, Chunk west) {
        if (y < 0) return true;
        if (y >= CHUNK_HEIGHT) return false;
        if (x < 0) return isSolid(south, x + CHUNK_SIZE, y, z);
        if (x >= CHUNK_SIZE) return isSolid(north, x - CHUNK_SIZE, y, z);
        if (z < 0) return isSolid(west, x, y, z + CHUNK_SIZE);
        if (z >= CHUNK_SIZE) return isSolid(east, x, y, z - CHUNK_SIZE);
        return blockAt(x, y, z) != 0;
    }

    public int[] buildVertexData() {
        Chunk north = neighbour(1, 0);
        Chunk south = neighbour(-1, 0);
        Chunk east = neighbour(0, 1);
        Chunk west = neighbour(0, -1);

        int[] buffer = SCRATCH.get();
        int count = 0;

        for (int sectionIndex = 0; sectionIndex < SECTION_COUNT; sectionIndex++) {
            byte[] section = sections[sectionIndex];
            if (section == null) continue;
            int baseY = sectionIndex * SECTION_HEIGHT;

            for (int i = 0; i < SECTION_VOLUME; i++) {
                byte block = section[i];
                if (block == 0) continue;
                int x = i & 15;
                int z = (i >> 4) & 15;
                int y = baseY + (i >> 8);

                for (int face = 0; face < BlockFaces.FACE_COUNT; face++) {
                    int[] normal = BlockFaces.FACE_NORMALS[face];
                    if (isNeighbourSolid(x + normal[0], y + normal[1], z + normal[2], north, south, east, west)) continue;

                    if (count + 4 > buffer.length) {
                        buffer = Arrays.copyOf(buffer, buffer.length * 2);
                        SCRATCH.set(buffer);
                    }

                    count = BlockFaces.emitFace(buffer, count, face, block, x, y, z);
                }
            }
        }

        return Arrays.copyOf(buffer, count);
    }

    public static int unpackX(int vertex) { return BlockFaces.unpackX(vertex); }
    public static int unpackY(int vertex) { return BlockFaces.unpackY(vertex); }
    public static int unpackZ(int vertex) { return BlockFaces.unpackZ(vertex); }
    public static float unpackU(int vertex) { return BlockFaces.unpackU(vertex); }
    public static float unpackV(int vertex) { return BlockFaces.unpackV(vertex); }

    public void buildMesh(){
        int[] data = buildVertexData();

        if (chunkMesh != null) {
            chunkMesh.cleanup();
            chunkMesh = null;
        }
        meshed = true;

        if (data.length == 0) {
            meshMinY = 0;
            meshMaxY = 0;
            return;
        }

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int vertex : data) {
            int y = unpackY(vertex);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        meshMinY = minY;
        meshMaxY = maxY;

        chunkMesh = new ChunkMesh(data, data.length / ChunkMesh.VERTICES_PER_QUAD);
    }

    public boolean hasMesh() {
        return meshed;
    }

    public boolean hasVisibleMesh() {
        return chunkMesh != null;
    }

    public int getMeshMinY() {
        return meshMinY;
    }

    public int getMeshMaxY() {
        return meshMaxY;
    }

    public boolean isOnBorder(int x, int z) {
        return x == 0 || x == CHUNK_SIZE - 1 || z == 0 || z == CHUNK_SIZE - 1;
    }

    public Vector3i getChunkPosition() { return chunkPosition; }

    public void render(){
        if (chunkMesh != null) {
            chunkMesh.draw();
        }
    }

    public void cleanup(){
        if (chunkMesh != null) {
            chunkMesh.cleanup();
            chunkMesh = null;
        }
    }

}
