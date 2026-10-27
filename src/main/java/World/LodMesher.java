package World;

import java.util.Arrays;

public final class LodMesher {

    private static final int COLUMNS = Chunk.CHUNK_SIZE;

    private LodMesher() {
    }

    // Builds a packed mesh of 16 x 16 columns, each voxel scale blocks wide, straight from the generator.
    // Positions are in voxels relative to the node's first chunk.
    public static int[] buildVertexData(LodNode node, TerrainGenerator generator) {
        int scale = node.voxelScale();
        int originX = node.minChunkX() * Chunk.CHUNK_SIZE;
        int originZ = node.minChunkZ() * Chunk.CHUNK_SIZE;

        int[][] heights = new int[COLUMNS + 2][COLUMNS + 2];
        for (int i = 0; i < COLUMNS + 2; i++) {
            for (int k = 0; k < COLUMNS + 2; k++) {
                int worldX = originX + (i - 1) * scale + scale / 2;
                int worldZ = originZ + (k - 1) * scale + scale / 2;
                heights[i][k] = generator.getHeight(worldX, worldZ);
            }
        }

        int[] buffer = new int[COLUMNS * COLUMNS * 24];
        int count = 0;

        for (int i = 0; i < COLUMNS; i++) {
            for (int k = 0; k < COLUMNS; k++) {
                int height = heights[i + 1][k + 1];
                int slope = Math.max(
                        Math.abs(heights[i + 2][k + 1] - heights[i][k + 1]),
                        Math.abs(heights[i + 1][k + 2] - heights[i + 1][k])) / scale;
                TerrainGenerator.Column column = generator.describeColumn(
                        originX + i * scale + scale / 2, originZ + k * scale + scale / 2, height, slope);
                int top = voxelHeight(height, scale);

                if (count + 4 > buffer.length) buffer = Arrays.copyOf(buffer, buffer.length * 2);
                count = BlockFaces.emitFace(buffer, count, 0, blockAt(column, top, top - 1, scale), i, top - 1, k);

                for (int face = 2; face < BlockFaces.FACE_COUNT; face++) {
                    int[] normal = BlockFaces.FACE_NORMALS[face];
                    int ni = i + normal[0];
                    int nk = k + normal[2];
                    int neighbourTop = voxelHeight(heights[ni + 1][nk + 1], scale);
                    boolean border = ni < 0 || ni >= COLUMNS || nk < 0 || nk >= COLUMNS;
                    // A one voxel skirt at region edges covers gaps against neighbours at other detail levels
                    int exposedFrom = border ? Math.max(0, neighbourTop - 1) : neighbourTop;

                    for (int vy = exposedFrom; vy < top; vy++) {
                        if (count + 4 > buffer.length) buffer = Arrays.copyOf(buffer, buffer.length * 2);
                        count = BlockFaces.emitFace(buffer, count, face, blockAt(column, top, vy, scale), i, vy, k);
                    }
                }
            }
        }
        return Arrays.copyOf(buffer, count);
    }

    // Rounds up so coarse terrain never sits below the full-detail surface it replaces
    public static int voxelHeight(int heightInBlocks, int scale) {
        return Math.max(1, (heightInBlocks + scale - 1) / scale);
    }

    private static int blockAt(TerrainGenerator.Column column, int top, int voxelY, int scale) {
        return column.blockAtDepth((top - 1 - voxelY) * scale).ordinal();
    }
}
