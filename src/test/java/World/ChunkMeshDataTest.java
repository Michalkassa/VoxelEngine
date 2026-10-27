package World;

import Core.TextureAtlas;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChunkMeshDataTest {

    private static final byte STONE = (byte) Block.STONE.ordinal();
    private static final byte GRASS = (byte) Block.GRASS.ordinal();

    private Map<Vector3i, Chunk> chunks;
    private ChunkManager chunkManager;

    @BeforeEach
    void setUp() {
        chunks = new HashMap<>();
        chunkManager = new ChunkManager(0) {
            @Override
            public Chunk getChunk(Vector3i position) {
                return chunks.get(position);
            }
        };
    }

    private Chunk addChunk(int cx, int cz, byte[][][] blocks) {
        Chunk chunk = new Chunk(new Vector3i(cx, 0, cz), chunkManager, blocks);
        chunks.put(new Vector3i(cx, 0, cz), chunk);
        return chunk;
    }

    private static byte[][][] emptyBlocks() {
        return new byte[Chunk.CHUNK_SIZE][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE];
    }

    private static int quadCount(int[] data) {
        assertEquals(0, data.length % 4);
        return data.length / 4;
    }

    private static Vector3f corner(int vertex) {
        return new Vector3f(Chunk.unpackX(vertex), Chunk.unpackY(vertex), Chunk.unpackZ(vertex));
    }

    private static Vector3f quadNormal(int[] data, int quad) {
        Vector3f a = corner(data[quad * 4]);
        Vector3f b = corner(data[quad * 4 + 1]);
        Vector3f c = corner(data[quad * 4 + 2]);
        return new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
    }

    @Test
    void emptyChunkHasNoFaces() {
        assertEquals(0, addChunk(0, 0, emptyBlocks()).buildVertexData().length);
    }

    @Test
    void floatingBlockHasSixFaces() {
        byte[][][] blocks = emptyBlocks();
        blocks[5][100][5] = STONE;

        assertEquals(6, quadCount(addChunk(0, 0, blocks).buildVertexData()));
    }

    @Test
    void bottomFaceAtWorldFloorIsSkipped() {
        byte[][][] blocks = emptyBlocks();
        blocks[5][0][5] = STONE;

        assertEquals(5, quadCount(addChunk(0, 0, blocks).buildVertexData()));
    }

    @Test
    void sharedFaceBetweenTouchingBlocksIsHidden() {
        byte[][][] blocks = emptyBlocks();
        blocks[5][100][5] = STONE;
        blocks[6][100][5] = STONE;

        assertEquals(10, quadCount(addChunk(0, 0, blocks).buildVertexData()));
    }

    @Test
    void fullyBuriedBlockHasNoFaces() {
        byte[][][] blocks = emptyBlocks();
        for (int x = 4; x <= 6; x++)
            for (int y = 99; y <= 101; y++)
                for (int z = 4; z <= 6; z++)
                    blocks[x][y][z] = STONE;

        assertEquals(9 * 6, quadCount(addChunk(0, 0, blocks).buildVertexData()));
    }

    @Test
    void faceAgainstSolidNeighbourChunkIsHidden() {
        byte[][][] here = emptyBlocks();
        here[15][100][5] = STONE;
        byte[][][] east = emptyBlocks();
        east[0][100][5] = STONE;
        Chunk chunk = addChunk(0, 0, here);
        addChunk(1, 0, east);

        assertEquals(5, quadCount(chunk.buildVertexData()));
    }

    @Test
    void faceAgainstEmptyNeighbourChunkIsShown() {
        byte[][][] here = emptyBlocks();
        here[0][100][0] = STONE;
        Chunk chunk = addChunk(0, 0, here);
        addChunk(-1, 0, emptyBlocks());
        addChunk(0, -1, emptyBlocks());

        assertEquals(6, quadCount(chunk.buildVertexData()));
    }

    @Test
    void faceAgainstMissingNeighbourChunkIsShown() {
        byte[][][] here = emptyBlocks();
        here[0][100][15] = STONE;

        assertEquals(6, quadCount(addChunk(0, 0, here).buildVertexData()));
    }

    @Test
    void neighbourLookupUsesCorrectSideForEveryDirection() {
        byte[][][] here = emptyBlocks();
        here[0][100][0] = STONE;
        here[15][100][15] = STONE;
        Chunk chunk = addChunk(0, 0, here);

        byte[][][] negativeX = emptyBlocks();
        negativeX[15][100][0] = STONE;
        addChunk(-1, 0, negativeX);
        byte[][][] negativeZ = emptyBlocks();
        negativeZ[0][100][15] = STONE;
        addChunk(0, -1, negativeZ);
        byte[][][] positiveX = emptyBlocks();
        positiveX[0][100][15] = STONE;
        addChunk(1, 0, positiveX);
        byte[][][] positiveZ = emptyBlocks();
        positiveZ[15][100][0] = STONE;
        addChunk(0, 1, positiveZ);

        assertEquals(12 - 4, quadCount(chunk.buildVertexData()));
    }

    @Test
    void verticesStayInsideTheirBlock() {
        byte[][][] blocks = emptyBlocks();
        blocks[3][70][12] = STONE;

        for (int vertex : addChunk(0, 0, blocks).buildVertexData()) {
            int x = Chunk.unpackX(vertex), y = Chunk.unpackY(vertex), z = Chunk.unpackZ(vertex);
            assertTrue(x == 3 || x == 4, "x " + x);
            assertTrue(y == 70 || y == 71, "y " + y);
            assertTrue(z == 12 || z == 13, "z " + z);
        }
    }

    @Test
    void cornerVerticesFitAtChunkMaximum() {
        byte[][][] blocks = emptyBlocks();
        blocks[15][255][15] = STONE;

        Set<String> corners = new HashSet<>();
        for (int vertex : addChunk(0, 0, blocks).buildVertexData()) {
            corners.add(Chunk.unpackX(vertex) + "," + Chunk.unpackY(vertex) + "," + Chunk.unpackZ(vertex));
        }

        assertTrue(corners.contains("16,256,16"));
        assertEquals(8, corners.size());
    }

    @Test
    void everyFaceWindsCounterClockwiseFacingOutward() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;
        int[] data = addChunk(0, 0, blocks).buildVertexData();
        Vector3f center = new Vector3f(8.5f, 100.5f, 8.5f);

        for (int quad = 0; quad < quadCount(data); quad++) {
            Vector3f faceCenter = new Vector3f();
            for (int i = 0; i < 4; i++) faceCenter.add(corner(data[quad * 4 + i]));
            faceCenter.div(4);
            Vector3f outward = faceCenter.sub(center).normalize();

            assertEquals(1f, quadNormal(data, quad).dot(outward), 1e-5f, "quad " + quad);
        }
    }

    @Test
    void quadCornersFormSquareWithSharedDiagonal() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;
        int[] data = addChunk(0, 0, blocks).buildVertexData();

        for (int quad = 0; quad < quadCount(data); quad++) {
            Vector3f a = corner(data[quad * 4]);
            Vector3f c = corner(data[quad * 4 + 2]);
            assertEquals(Math.sqrt(2), a.distance(c), 1e-5, "diagonal of quad " + quad);
        }
    }

    @Test
    void texturesComeFromAtlasPerFace() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = GRASS;
        int[] data = addChunk(0, 0, blocks).buildVertexData();
        float[][] top = TextureAtlas.topTextureMapping(TextureAtlas.BlockTextures.GRASS);
        float[][] side = TextureAtlas.sideTextureMapping(TextureAtlas.BlockTextures.GRASS);

        int topQuads = 0, sideQuads = 0;
        for (int quad = 0; quad < quadCount(data); quad++) {
            Vector3f normal = quadNormal(data, quad);
            float[][] expected = normal.y > 0.5f ? top : Math.abs(normal.y) < 0.5f ? side : null;
            if (expected == null) continue;
            if (expected == top) topQuads++; else sideQuads++;
            int[] corners = {0, 1, 2, 4};
            for (int i = 0; i < 4; i++) {
                int vertex = data[quad * 4 + i];
                assertEquals(expected[corners[i]][0], Chunk.unpackU(vertex), 1e-6f);
                assertEquals(expected[corners[i]][1], Chunk.unpackV(vertex), 1e-6f);
            }
        }
        assertEquals(1, topQuads);
        assertEquals(4, sideQuads);
    }

    @Test
    void generatedTerrainFaceCountMatchesBruteForce() {
        TerrainGenerator generator = new TerrainGenerator(7);
        Map<Vector3i, byte[][][]> raw = new HashMap<>();
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                byte[][][] blocks = generator.generateChunkTerrain(new Vector3i(cx, 0, cz));
                raw.put(new Vector3i(cx, 0, cz), blocks);
                addChunk(cx, cz, blocks);
            }
        }

        int expected = 0;
        int[][] directions = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        byte[][][] center = raw.get(new Vector3i(0, 0, 0));
        for (int x = 0; x < 16; x++) for (int y = 0; y < 256; y++) for (int z = 0; z < 16; z++) {
            if (center[x][y][z] == 0) continue;
            for (int[] d : directions) {
                int nx = x + d[0], ny = y + d[1], nz = z + d[2];
                if (ny < 0) continue;
                if (ny > 255) { expected++; continue; }
                int cx = Math.floorDiv(nx, 16), cz = Math.floorDiv(nz, 16);
                if (raw.get(new Vector3i(cx, 0, cz))[Math.floorMod(nx, 16)][ny][Math.floorMod(nz, 16)] == 0) expected++;
            }
        }

        assertEquals(expected, quadCount(chunks.get(new Vector3i(0, 0, 0)).buildVertexData()));
    }

    // Ambient occlusion of each corner of the top face of the block at (x, y, z), keyed by "x,z" of the corner
    private static Map<String, Integer> topFaceAo(int[] data, int x, int y, int z) {
        Map<String, Integer> ao = new HashMap<>();
        for (int quad = 0; quad < quadCount(data); quad++) {
            if (quadNormal(data, quad).y < 0.5f) continue;
            int first = data[quad * 4];
            if (Chunk.unpackY(first) != y + 1) continue;
            Set<String> corners = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                int vertex = data[quad * 4 + i];
                corners.add(Chunk.unpackX(vertex) + "," + Chunk.unpackZ(vertex));
            }
            if (!corners.contains(x + "," + z) || !corners.contains((x + 1) + "," + (z + 1))) continue;
            for (int i = 0; i < 4; i++) {
                int vertex = data[quad * 4 + i];
                ao.put(Chunk.unpackX(vertex) + "," + Chunk.unpackZ(vertex), Chunk.unpackAo(vertex));
            }
        }
        assertEquals(4, ao.size(), "top face of " + x + "," + y + "," + z);
        return ao;
    }

    @Test
    void ambientOcclusionLevels() {
        assertEquals(3, BlockFaces.ambientOcclusion(false, false, false));
        assertEquals(2, BlockFaces.ambientOcclusion(false, false, true));
        assertEquals(2, BlockFaces.ambientOcclusion(true, false, false));
        assertEquals(1, BlockFaces.ambientOcclusion(true, false, true));
        assertEquals(0, BlockFaces.ambientOcclusion(true, true, false));
        assertEquals(0, BlockFaces.ambientOcclusion(true, true, true));
    }

    @Test
    void isolatedBlockIsFullyLit() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;

        for (int vertex : addChunk(0, 0, blocks).buildVertexData()) {
            assertEquals(3, Chunk.unpackAo(vertex));
        }
    }

    @Test
    void wallBesideFloorDarkensTheSharedEdge() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;
        blocks[9][101][8] = STONE;

        Map<String, Integer> ao = topFaceAo(addChunk(0, 0, blocks).buildVertexData(), 8, 100, 8);

        assertEquals(2, ao.get("9,8"));
        assertEquals(2, ao.get("9,9"));
        assertEquals(3, ao.get("8,8"));
        assertEquals(3, ao.get("8,9"));
    }

    @Test
    void cornerBetweenTwoWallsIsFullyDark() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;
        blocks[9][101][8] = STONE;
        blocks[8][101][9] = STONE;

        Map<String, Integer> ao = topFaceAo(addChunk(0, 0, blocks).buildVertexData(), 8, 100, 8);

        assertEquals(0, ao.get("9,9"));
        assertEquals(2, ao.get("9,8"));
        assertEquals(2, ao.get("8,9"));
        assertEquals(3, ao.get("8,8"));
    }

    @Test
    void ambientOcclusionReadsDiagonalNeighbourChunk() {
        byte[][][] here = emptyBlocks();
        here[15][100][15] = STONE;
        byte[][][] diagonal = emptyBlocks();
        diagonal[0][101][0] = STONE;
        Chunk chunk = addChunk(0, 0, here);
        addChunk(1, 1, diagonal);

        Map<String, Integer> ao = topFaceAo(chunk.buildVertexData(), 15, 100, 15);

        assertEquals(2, ao.get("16,16"));
        assertEquals(3, ao.get("15,15"));
    }

    @Test
    void quadSplitsAlongTheBrighterDiagonal() {
        byte[][][] blocks = emptyBlocks();
        blocks[8][100][8] = STONE;
        blocks[9][101][9] = STONE;
        int[] data = addChunk(0, 0, blocks).buildVertexData();

        for (int quad = 0; quad < quadCount(data); quad++) {
            if (quadNormal(data, quad).y < 0.5f || Chunk.unpackY(data[quad * 4]) != 101) continue;
            int[] ao = new int[4];
            for (int i = 0; i < 4; i++) ao[i] = Chunk.unpackAo(data[quad * 4 + i]);
            assertTrue(ao[0] + ao[2] >= ao[1] + ao[3], "quad " + quad + " splits through the dark corner");
            return;
        }
        fail("top face not found");
    }

    @Test
    void repeatedBuildsReturnIdenticalData() {
        byte[][][] blocks = new TerrainGenerator(3).generateChunkTerrain(new Vector3i());
        Chunk chunk = addChunk(0, 0, blocks);

        assertArrayEquals(chunk.buildVertexData(), chunk.buildVertexData());
    }
}
