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

    // Packed vertex: x (5 bits) | y (9 bits) | z (5 bits) | u * 16 (5 bits) | v * 16 (5 bits) | ao (2 bits)
    private static final int Y_SHIFT = 5;
    private static final int Z_SHIFT = 14;
    private static final int UV_SHIFT = 19;
    private static final int UV_SCALE = 16;
    private static final int AO_SHIFT = 29;

    // Ambient occlusion per corner: 0 is fully enclosed, 3 is fully open
    static final int AO_OPEN = 3;
    private static final int[] NO_OCCLUSION = { AO_OPEN, AO_OPEN, AO_OPEN, AO_OPEN };

    private static final int[][][] FACE_CORNER_OFFSETS = new int[FACES.length][4][3];
    // For each face corner, the blocks in front of the face that can shade it: side 1, side 2 and the diagonal
    private static final int[][][][] AO_SAMPLE_OFFSETS = new int[FACES.length][4][3][3];
    private static final int[][][] PACKED_UVS = buildPackedUvs();

    static {
        for (int face = 0; face < FACES.length; face++) {
            for (int corner = 0; corner < 4; corner++) {
                float[] vertex = FACES[face][QUAD_CORNERS[corner]];
                for (int axis = 0; axis < 3; axis++) {
                    FACE_CORNER_OFFSETS[face][corner][axis] = Math.round(vertex[axis] + 0.5f);
                }
                buildAoSamples(face, corner);
            }
        }
    }

    // The samples sit in the layer of blocks the face looks into, stepping from the face centre towards the corner
    private static void buildAoSamples(int face, int corner) {
        int[] normal = FACE_NORMALS[face];
        int[] offset = FACE_CORNER_OFFSETS[face][corner];
        int[][] samples = AO_SAMPLE_OFFSETS[face][corner];
        int side = 0;
        for (int axis = 0; axis < 3; axis++) {
            int towardsCorner = normal[axis] != 0 ? normal[axis] : offset[axis] * 2 - 1;
            samples[2][axis] = towardsCorner;
            for (int sample = 0; sample < 2; sample++) {
                samples[sample][axis] = normal[axis];
            }
            if (normal[axis] == 0) {
                samples[side++][axis] = towardsCorner;
            }
        }
    }

    // Two solid sides already hide the corner, whatever the diagonal block is
    static int ambientOcclusion(boolean side1, boolean side2, boolean diagonal) {
        if (side1 && side2) return 0;
        return AO_OPEN - ((side1 ? 1 : 0) + (side2 ? 1 : 0) + (diagonal ? 1 : 0));
    }

    interface Solidity {
        boolean isSolid(int x, int y, int z);
    }

    // Fills ao with the ambient occlusion of the 4 corners of one face of the block at (x, y, z)
    static void faceAmbientOcclusion(int face, int x, int y, int z, Solidity solidity, int[] ao) {
        for (int corner = 0; corner < 4; corner++) {
            int[][] samples = AO_SAMPLE_OFFSETS[face][corner];
            ao[corner] = ambientOcclusion(
                    solidity.isSolid(x + samples[0][0], y + samples[0][1], z + samples[0][2]),
                    solidity.isSolid(x + samples[1][0], y + samples[1][1], z + samples[1][2]),
                    solidity.isSolid(x + samples[2][0], y + samples[2][1], z + samples[2][2]));
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
        return pack(x, y, z, packedUv, AO_OPEN);
    }

    static int pack(int x, int y, int z, int packedUv, int ao) {
        return x | (y << Y_SHIFT) | (z << Z_SHIFT) | (packedUv << UV_SHIFT) | (ao << AO_SHIFT);
    }

    // Writes the 4 corners of one face of the block at (x, y, z) scaled by 1, returning the new count
    static int emitFace(int[] buffer, int count, int face, int block, int x, int y, int z) {
        return emitFace(buffer, count, face, block, x, y, z, NO_OCCLUSION);
    }

    static int emitFace(int[] buffer, int count, int face, int block, int x, int y, int z, int[] ao) {
        int[] uvs = PACKED_UVS[block < PACKED_UVS.length ? block : 0][FACE_TEXTURE[face]];
        // The quad is split along corners 0-2. When the other diagonal is brighter, start at corner 1
        // so the split runs along it instead, otherwise the shading on the face looks lopsided.
        int first = ao[0] + ao[2] < ao[1] + ao[3] ? 1 : 0;
        for (int i = 0; i < 4; i++) {
            int corner = (first + i) & 3;
            int[] offset = FACE_CORNER_OFFSETS[face][corner];
            buffer[count++] = pack(x + offset[0], y + offset[1], z + offset[2], uvs[corner], ao[corner]);
        }
        return count;
    }

    static int unpackX(int vertex) { return vertex & 31; }
    static int unpackY(int vertex) { return (vertex >>> Y_SHIFT) & 511; }
    static int unpackZ(int vertex) { return (vertex >>> Z_SHIFT) & 31; }
    static float unpackU(int vertex) { return ((vertex >>> UV_SHIFT) & 31) / (float) UV_SCALE; }
    static float unpackV(int vertex) { return ((vertex >>> (UV_SHIFT + 5)) & 31) / (float) UV_SCALE; }
    static int unpackAo(int vertex) { return (vertex >>> AO_SHIFT) & 3; }
}
