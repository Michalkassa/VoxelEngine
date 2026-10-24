package World;

import Core.TextureAtlas;

final class BlockFaces {

    private BlockFaces() {
    }

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

    private static final float[][][] FACES = {
            CUBE_TOP_FACE, CUBE_BOTTOM_FACE, CUBE_NORTH_FACE, CUBE_SOUTH_FACE, CUBE_EAST_FACE, CUBE_WEST_FACE
    };
    static final int[][] FACE_NORMALS = {
            { 0, 1, 0 }, { 0, -1, 0 }, { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 }
    };
    private static final int TOP = 0, BOTTOM = 1, SIDE = 2;
    private static final int[] FACE_TEXTURE = { TOP, BOTTOM, SIDE, SIDE, SIDE, SIDE };

    // The 6-vertex faces repeat vertex 2 and 0, so vertices 0, 1, 2 and 4 are the quad corners
    private static final int[] QUAD_CORNERS = { 0, 1, 2, 4 };

    // Packed vertex: x (5 bits) | y (9 bits) | z (5 bits) | u * 16 (5 bits) | v * 16 (5 bits)
    private static final int Y_SHIFT = 5;
    private static final int Z_SHIFT = 14;
    private static final int UV_SHIFT = 19;
    private static final int UV_SCALE = 16;

    private static final int[][][] FACE_CORNER_OFFSETS = new int[FACES.length][4][3];
    private static final int[][][] PACKED_UVS = buildPackedUvs();

    static {
        for (int face = 0; face < FACES.length; face++) {
            for (int corner = 0; corner < 4; corner++) {
                float[] vertex = FACES[face][QUAD_CORNERS[corner]];
                for (int axis = 0; axis < 3; axis++) {
                    FACE_CORNER_OFFSETS[face][corner][axis] = Math.round(vertex[axis] + 0.5f);
                }
            }
        }
    }

    static final int FACE_COUNT = FACES.length;

    private static int[][][] buildPackedUvs() {
        Block[] blocks = Block.values();
        TextureAtlas.BlockTextures[] textures = TextureAtlas.BlockTextures.values();
        int[][][] uvs = new int[blocks.length][3][4];
        for (int block = 1; block < blocks.length && block - 1 < textures.length; block++) {
            TextureAtlas.BlockTextures texture = textures[block - 1];
            float[][][] mappings = {
                    TextureAtlas.topTextureMapping(texture),
                    TextureAtlas.bottomTextureMapping(texture),
                    TextureAtlas.sideTextureMapping(texture)
            };
            for (int kind = 0; kind < 3; kind++) {
                for (int corner = 0; corner < 4; corner++) {
                    float[] uv = mappings[kind][QUAD_CORNERS[corner]];
                    int u = Math.round(uv[0] * UV_SCALE);
                    int v = Math.round(uv[1] * UV_SCALE);
                    uvs[block][kind][corner] = u | (v << 5);
                }
            }
        }
        return uvs;
    }

    static int pack(int x, int y, int z, int packedUv) {
        return x | (y << Y_SHIFT) | (z << Z_SHIFT) | (packedUv << UV_SHIFT);
    }

    // Writes the 4 corners of one face of the block at (x, y, z) scaled by 1, returning the new count
    static int emitFace(int[] buffer, int count, int face, int block, int x, int y, int z) {
        int[] uvs = PACKED_UVS[block < PACKED_UVS.length ? block : 0][FACE_TEXTURE[face]];
        for (int corner = 0; corner < 4; corner++) {
            int[] offset = FACE_CORNER_OFFSETS[face][corner];
            buffer[count++] = pack(x + offset[0], y + offset[1], z + offset[2], uvs[corner]);
        }
        return count;
    }

    static int unpackX(int vertex) { return vertex & 31; }
    static int unpackY(int vertex) { return (vertex >>> Y_SHIFT) & 511; }
    static int unpackZ(int vertex) { return (vertex >>> Z_SHIFT) & 31; }
    static float unpackU(int vertex) { return ((vertex >>> UV_SHIFT) & 31) / (float) UV_SCALE; }
    static float unpackV(int vertex) { return ((vertex >>> (UV_SHIFT + 5)) & 31) / (float) UV_SCALE; }
}
